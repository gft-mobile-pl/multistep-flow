package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.StepType

open class GetCurrentStepUseCase<FlowStepType : StepType<*, *, *, *>>(private val flow: MultiStepFlow<FlowStepType>) {
    operator fun invoke() = flow.session.data.value?.currentStep
        ?: throw IllegalStateException("The flow has not started yet or has ended already")
}