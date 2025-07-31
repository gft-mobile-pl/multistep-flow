package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.getStepFromHistory

class GoBackToStep<FlowStepType : StepType<*, *, *, *>> internal constructor(private val flow: MultiStepFlow<FlowStepType>) {
    private val setStep = SetStep(flow)

    suspend operator fun <StepType : FlowStepType> invoke(stepType: StepType) {
        @Suppress("UNCHECKED_CAST")
        this(flow.getStepFromHistory(stepType) as Step<FlowStepType, *, *, *, *>)
    }

    suspend operator fun <StepType : FlowStepType> invoke(step: Step<StepType, *, *, *, *>) {
        setStep(
            step = step,
            reuseUserInput = false,
            clearHistoryTo = step,
            clearHistoryInclusive = true
        )
    }
}
