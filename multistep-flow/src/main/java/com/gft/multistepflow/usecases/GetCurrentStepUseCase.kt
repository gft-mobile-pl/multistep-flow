package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType

open class GetCurrentStepUseCase<FlowStepType : StepType<*, *, *, *>>(private val flow: MultiStepFlow<FlowStepType>) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): Step<FlowStepType, *, *, *, *> = (flow.session.data.value?.currentStep as Step<FlowStepType, *, *, *, *>?)
        ?: throw IllegalStateException("The flow has not started yet or has ended already")
}