package com.gft.multistepflow.model

import com.gft.observablesession.Session
import kotlinx.coroutines.sync.Mutex

open class MultiStepFlow<FlowStepType : StepType<*, *, *, *>>(
    val historyEnabled: Boolean,
    internal val mutex: Mutex = Mutex()
) {
    internal val session: Session<FlowState<*, *, *, *>> = Session()

    override fun toString(): String {
        return "${this::class.simpleName}(" +
            "isAnyOperationInProgress=${session.data.value?.isAnyOperationInProgress ?: false}, " +
            "currentStep=${session.data.value?.currentStep ?: "[none]"}, " +
            "stepsHistory=${session.data.value?.stepsHistory?.map { step -> step.type::class.simpleName } ?: "[none]"})"
    }
}

class FlowState<Type : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult>(
    val currentStep: Step<Type, Payload, UserInput, ValidationResult, *>,
    val isAnyOperationInProgress: Boolean,
    val stepsHistory: List<Step<*, *, *, *, *>>
) {
    internal fun copy(
        currentStep: Step<*, *, *, *, *> = this.currentStep,
        isAnyOperationInProgress: Boolean = this.isAnyOperationInProgress,
        stepsHistory: List<Step<*, *, *, *, *>> = this.stepsHistory
    ) = FlowState(
        currentStep = currentStep,
        isAnyOperationInProgress = isAnyOperationInProgress,
        stepsHistory = stepsHistory
    )

    override fun toString(): String {
        return "FlowState(currentStep=$currentStep, isAnyOperationInProgress=$isAnyOperationInProgress, stepsHistory=$stepsHistory)"
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
