package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType

open class GoBackToStepUseCase<FlowStepType : StepType<*, *, *, *>>(private val flow: MultiStepFlow<FlowStepType>) {
    private val setStep = SetStepUseCase(flow)
    private val getStepFromHistory = GetStepFromHistoryUseCase(flow)

    operator fun <StepType : FlowStepType> invoke(stepType: StepType) {
        @Suppress("UNCHECKED_CAST")
        this(getStepFromHistory(stepType) as Step<FlowStepType, *, *, *, *>)
    }

    operator fun <StepType : FlowStepType> invoke(step: Step<StepType, *, *, *, *>) {
        setStep(
            step = step,
            reuseUserInput = false,
            clearHistoryTo = step,
            clearHistoryInclusive = true
        )
    }
}
