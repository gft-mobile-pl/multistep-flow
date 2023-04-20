package com.gft.multistepflow.model

import com.gft.observablesession.Session
import kotlinx.coroutines.sync.Mutex

abstract class MultiStepFlow<FlowStepType : StepType<*, *, *, *>>(
    val localHistoryEnabled: Boolean = false,
    internal val mutex: Mutex = Mutex()
) {
    internal val session: Session<FlowState> = Session()

    override fun toString(): String {
        return "MultiStepFlow(isAnyOperationInProgress=${session.data.value?.isAnyOperationInProgress ?: false}, currentStep=${session.data.value?.currentStep ?: "[none]"})"
    }
}

internal data class FlowState(
    val currentStep: Step<*, *, *, *, *>,
    val isAnyOperationInProgress: Boolean,
    val previousSteps: List<Step<*, *, *, *, *>>
)