package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow

open class GetCurrentStepUseCase(private val flow: MultiStepFlow<*>) {
    operator fun invoke() = flow.session.data.value?.currentStep
        ?: throw IllegalStateException("The flow has not started yet or has ended already")
}