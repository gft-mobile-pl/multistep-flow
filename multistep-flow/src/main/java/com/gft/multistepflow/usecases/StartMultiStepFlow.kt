package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.FlowState
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.sync.withLock

open class StartMultiStepFlow<FlowStepType : StepType<*, *, *, *>>(
    private val flow: MultiStepFlow<FlowStepType>
) {
    suspend operator fun <StepType : FlowStepType> invoke(
        currentStep: Step<StepType, *, *, *, *>
    ) = flow.mutex.withLock {
        flow.session.start(
            FlowState(
                currentStep = currentStep,
                isAnyOperationInProgress = false,
                previousSteps = emptyList()
            )
        )
    }
}
