package com.gft.multistepflow.operations

import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import kotlinx.coroutines.sync.withLock

internal class StartFlow<FlowStepType : StepType<*, *, *, *>>(
    private val flow: MultiStepFlow<FlowStepType>,
) {
    suspend operator fun invoke(
        initialStep: Step<out FlowStepType, *, *, *, *>,
        assertFlowIsNotStarted: Boolean,
    ) = flow.mutex.withLock {
        if (flow.session.isStarted) {
            if (assertFlowIsNotStarted) throw IllegalStateException("Flow $this is already started!")
            return@withLock
        }
        initialStep.flow = flow
        flow.session.start(
            FlowState(
                currentStep = initialStep as Step<*, *, *, *, *>,
                isAnyOperationInProgress = false,
                stepsHistory = if (flow.historyEnabled) listOf(initialStep) else emptyList()
            )
        )
    }
}
