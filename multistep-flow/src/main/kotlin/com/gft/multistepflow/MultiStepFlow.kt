package com.gft.multistepflow

import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.operations.EndFlow
import com.gft.multistepflow.operations.StartFlow
import com.gft.observablesession.Session
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex

open class MultiStepFlow<FlowStepType : StepType<*, *, *, *>>(
    val historyEnabled: Boolean,
    internal val mutex: Mutex = Mutex(),
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
                return sessionId.hashCode() ?: 0
            }
        }

        override val replayCache: List<State>
            get() = flowState.replayCache.map { flowState -> flowState.resolveLifecycleState() }

        override val value: State
            get() = flowState.value.resolveLifecycleState()

        override suspend fun collect(collector: FlowCollector<State>): Nothing {
            flowState.collect { flowState -> flowState.resolveLifecycleState() }
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

val MultiStepFlow<*>.end: EndFlow
    get() = EndFlow(this)

class FlowState<Type : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult>(
    val currentStep: Step<Type, Payload, UserInput, ValidationResult, *>,
    internal val currentActionJob: Job?,
    val stepsHistory: List<Step<*, *, *, *, *>>,
    val lifecycleState: Lifecycle.State,
) {
    val isAnyOperationInProgress: Boolean = currentActionJob != null

    internal fun copy(
        currentStep: Step<*, *, *, *, *> = this.currentStep,
        currentActionJob: Job? = this.currentActionJob,
        stepsHistory: List<Step<*, *, *, *, *>> = this.stepsHistory,
        lifecycleState: Lifecycle.State = this.lifecycleState,
    ) = FlowState(
        currentStep = currentStep,
        currentActionJob = currentActionJob,
        stepsHistory = stepsHistory,
        lifecycleState = lifecycleState,
    )

    override fun toString(): String {
        return "FlowState(currentStep=$currentStep, isAnyOperationInProgress=$isAnyOperationInProgress, lifecycleState=$lifecycleState, stepsHistory=$stepsHistory)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlowState<*, *, *, *>

        if (currentStep != other.currentStep) return false
        if (isAnyOperationInProgress != other.isAnyOperationInProgress) return false
        if (stepsHistory != other.stepsHistory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentStep.hashCode()
        result = 31 * result + isAnyOperationInProgress.hashCode()
        result = 31 * result + stepsHistory.hashCode()
        return result
    }
}
