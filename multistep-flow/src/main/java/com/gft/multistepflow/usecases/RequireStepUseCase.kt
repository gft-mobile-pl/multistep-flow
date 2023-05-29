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

    @JvmName("invoke_Same_Payload")
    operator fun <T : StepType<Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, out UserInput, out ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")

    @JvmName("invoke_Same_Payload_UserInput")
    operator fun <T : StepType<Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, UserInput, out ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")

    @JvmName("invoke_Same_Payload_ValidationResult")
    operator fun <T : StepType<Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, out UserInput, ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")

    @JvmName("invoke_Same_Payload_UserInput_ValidationResult")
    operator fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, UserInput, ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")

    @JvmName("invoke_Same_UserInput")
    operator fun <T : StepType<out Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, UserInput, out ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")

    @JvmName("invoke_Same_UserInput_ValidationResult")
    operator fun <T : StepType<out Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, UserInput, ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")

    @JvmName("invoke_Same_ValidationResult")
    operator fun <T : StepType<out Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, ValidationResult, *> =
        flow.session.requireData().currentStep.castOrNull(stepType, *stepTypes)
            ?: throw IllegalArgumentException("Current step does not match the requested type")
}