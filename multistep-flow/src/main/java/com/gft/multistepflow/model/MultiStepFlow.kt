package com.gft.multistepflow.model

import com.gft.observablesession.Session
import kotlinx.coroutines.sync.Mutex

open class MultiStepFlow<FlowStepType : StepType<*, *, *, *>>(
    val historyEnabled: Boolean,
    internal val mutex: Mutex = Mutex()
) {
    internal val session: Session<FlowState<*, *, *, *>> = Session()

    override fun toString(): String {
        return "MultiStepFlow(isAnyOperationInProgress=${session.data.value?.isAnyOperationInProgress ?: false}, currentStep=${session.data.value?.currentStep ?: "[none]"})"
    }
}

class FlowState<Type : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult>(
    val currentStep: Step<Type, Payload, UserInput, ValidationResult, *>,
    val isAnyOperationInProgress: Boolean,
    val previousSteps: List<Step<*, *, *, *, *>>
) {
    internal fun copy(
        currentStep: Step<*, *, *, *, *> = this.currentStep,
        isAnyOperationInProgress: Boolean = this.isAnyOperationInProgress,
        previousSteps: List<Step<*, *, *, *, *>> = this.previousSteps
    ) = FlowState(
        currentStep = currentStep,
        isAnyOperationInProgress = isAnyOperationInProgress,
        previousSteps = previousSteps
    )

    override fun toString(): String {
        return "FlowState(currentStep=$currentStep, isAnyOperationInProgress=$isAnyOperationInProgress, previousSteps=$previousSteps)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlowState<*, *, *, *>

        if (currentStep != other.currentStep) return false
        if (isAnyOperationInProgress != other.isAnyOperationInProgress) return false
        if (previousSteps != other.previousSteps) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentStep.hashCode()
        result = 31 * result + isAnyOperationInProgress.hashCode()
        result = 31 * result + previousSteps.hashCode()
        return result
    }
}
