package com.gft.multistepflow

import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.operations.AwaitStep
import com.gft.multistepflow.operations.ClearError
import com.gft.multistepflow.operations.ClearFlow
import com.gft.multistepflow.operations.GetStepFromHistory
import com.gft.multistepflow.operations.RequireState
import com.gft.multistepflow.operations.RequireStep
import com.gft.multistepflow.operations.StartFlow
import com.gft.multistepflow.operations.StreamFlowState
import com.gft.multistepflow.operations.UpdateUserInput
import com.gft.multistepflow.operations.WhenStep
import com.gft.observablesession.Session
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

open class MultiStepFlow<FlowStepType : StepType<*, *, *, *>>(
    val historyEnabled: Boolean,
) {
    internal val session: Session<FlowState<*, *, *, *>> = Session()

    val lifecycle: StateFlow<Lifecycle.State> = Lifecycle(session.data)

    class Lifecycle internal constructor(private val flowState: StateFlow<FlowState<*, *, *, *>?>) :
        StateFlow<Lifecycle.State> {
        sealed class State(val sessionId: String) {
            class Started internal constructor(sessionId: String) : State(sessionId)
            data object NotInitialized : State("")
            class Clearing internal constructor(sessionId: String) : State(sessionId)

            fun isStarted(): Boolean = this is Started

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is State) return false
                if (sessionId != other.sessionId) return false
                return true
            }

            override fun hashCode(): Int {
                return sessionId.hashCode()
            }

            override fun toString(): String = "${this::class.simpleName}(sessionId=$sessionId)"
        }

        override val replayCache: List<State>
            get() = flowState.replayCache.map { flowState -> flowState.resolveLifecycleState() }

        override val value: State
            get() = flowState.value.resolveLifecycleState()

        override suspend fun collect(collector: FlowCollector<State>): Nothing {
            flowState.collect { flowState -> collector.emit(flowState.resolveLifecycleState()) }
        }

        private fun FlowState<*, *, *, *>?.resolveLifecycleState() = this?.lifecycleState ?: State.NotInitialized
    }

    override fun toString(): String {
        return "${this::class.simpleName}(" +
                "isAnyOperationInProgress=${session.data.value?.isAnyOperationInProgress ?: false}, " +
                "currentStep=${session.data.value?.currentStep ?: "[none]"}, " +
                "lifecycleState=${lifecycle.value}, " +
                "stepsHistory=${session.data.value?.stepsHistory?.map { step -> step.type::class.simpleName } ?: "[none]"})"
    }
}

val <FlowStepType : StepType<*, *, *, *>> MultiStepFlow<FlowStepType>.start
    get() = StartFlow(this)

val MultiStepFlow<*>.requireStep
    get() = RequireStep(this)

val MultiStepFlow<*>.awaitStep
    get() = AwaitStep(this)

val MultiStepFlow<*>.whenStep
    get() = WhenStep(this)

val MultiStepFlow<*>.getStepFromHistory
    get() = GetStepFromHistory(this)

val <FlowStepType : StepType<*, *, *, *>> MultiStepFlow<FlowStepType>.requireState
    get() = RequireState(this)

val <FlowStepType : StepType<*, *, *, *>> MultiStepFlow<FlowStepType>.streamState
    get() = StreamFlowState(this)

val <FlowStepType : StepType<*, *, *, *>> MultiStepFlow<FlowStepType>.clear: ClearFlow
    get() = ClearFlow(this)

val MultiStepFlow<*>.clearError
    get() = ClearError(this)

val MultiStepFlow<*>.updateUserInput
    get() = UpdateUserInput(this)


class FlowState<Type : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult>(
    val currentStep: Step<Type, Payload, UserInput, ValidationResult, *>,
    internal val currentActionJob: Job?,
    val stepsHistory: List<Step<*, *, *, *, *>>,
    val lifecycleState: Lifecycle.State,
    val error: ActionError?,
) {
    val isAnyOperationInProgress: Boolean = currentActionJob != null

    internal fun copy(
        currentStep: Step<*, *, *, *, *> = this.currentStep,
        currentAction: Job? = this.currentActionJob,
        stepsHistory: List<Step<*, *, *, *, *>> = this.stepsHistory,
        lifecycleState: Lifecycle.State = this.lifecycleState,
        error: ActionError? = this.error,
    ) = FlowState(
        currentStep = currentStep,
        currentActionJob = currentAction,
        stepsHistory = stepsHistory,
        lifecycleState = lifecycleState,
        error = error
    )

    override fun toString(): String {
        return "FlowState(currentStep=$currentStep, isAnyOperationInProgress=$isAnyOperationInProgress, lifecycleState=$lifecycleState, error=$error, stepsHistory=$stepsHistory)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlowState<*, *, *, *>

        if (currentStep != other.currentStep) return false
        if (isAnyOperationInProgress != other.isAnyOperationInProgress) return false
        if (stepsHistory != other.stepsHistory) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentStep.hashCode()
        result = 31 * result + isAnyOperationInProgress.hashCode()
        result = 31 * result + stepsHistory.hashCode()
        result = 31 * result + error.hashCode()
        return result
    }
}
