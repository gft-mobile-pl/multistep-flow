package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.sync.withLock

class RestartFlowUseCase<FlowStepType : StepType<*, *, *, *>>(
    private val flow: MultiStepFlow<FlowStepType>,
) {
    suspend operator fun <StepType : FlowStepType> invoke(
        initialStep: Step<StepType, *, *, *, *>,
    ) = flow.mutex.withLock {
        if (!flow.session.isStarted) throw IllegalStateException("Flow $flow is not started.")
        flow.session.update { state ->
            state.copy(
                currentStep = initialStep,
                previousSteps = if (flow.historyEnabled) listOf(initialStep) else emptyList()
            )
        }
    }
}
