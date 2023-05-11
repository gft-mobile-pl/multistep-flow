package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.castOrNull

open class RequireStepUseCase(private val flow: MultiStepFlow<*>) {
    operator fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, out ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")
}