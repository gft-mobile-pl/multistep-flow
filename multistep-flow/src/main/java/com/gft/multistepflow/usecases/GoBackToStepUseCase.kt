package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType

open class GoBackToStepUseCase<FlowStepType : StepType<*, *, *, *>>(private val flow: MultiStepFlow<FlowStepType>) {
    private val setStep = SetStepUseCase(flow)
    private val getStepFromHistory = GetStepFromHistoryUseCase(flow)

    operator fun <T : StepType<Payload, UserInput, ValidationData, *>, Payload, UserInput, ValidationData> invoke(stepType: T) {
        @Suppress("UNCHECKED_CAST")
        this(getStepFromHistory(stepType) as Step<FlowStepType, *, *, *, *>)
    }

    operator fun invoke(step: Step<FlowStepType, *, *, *, *>) {
        setStep(
            step = step,
            reuseUserInput = false,
            clearHistoryTo = step,
            clearHistoryInclusive = true
        )
    }
}