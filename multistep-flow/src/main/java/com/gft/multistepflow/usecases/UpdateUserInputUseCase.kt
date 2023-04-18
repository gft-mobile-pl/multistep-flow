package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.MultiStepFlow.FlowPayload
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.validators.BaseUserInputValidator
import kotlin.reflect.KClass

abstract class UpdateUserInputUseCase(
    private val flow: MultiStepFlow
) {
    inline operator fun <reified Type : StepType<*, UserInput, *, *>, reified UserInput : Any> invoke(
        noinline mutator: (UserInput) -> UserInput
    ) {
        this(Type::class, mutator)
    }

    @Suppress("UNCHECKED_CAST")
    private operator fun <Type : StepType<*, UserInput, *, *>, UserInput> invoke(
        stepClass: KClass<Type>, mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->
            if (sessionData.currentStep.type != stepClass) {
                throw IllegalArgumentException("Cannot update current step as it cannot be casted to $stepClass")
            }
            val currentStep = sessionData.currentStep as Step<*, *, UserInput, Any?, BaseUserInputValidator<Any?, Any?, Any?>>
            updateSession(currentStep, mutator, sessionData)
        }
    }

    operator fun <UserInput, ValidationResult> invoke(
        vararg classes: KClass<out StepType<*, UserInput, ValidationResult, *>>,
        mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->
            for (stepClass in classes) {
                if (sessionData.currentStep.type == stepClass) {
                    @Suppress("UNCHECKED_CAST")
                    val currentStep = sessionData.currentStep as Step<*, *, UserInput, ValidationResult, BaseUserInputValidator<Any?, ValidationResult, ValidationResult>>
                    return@update updateSession(currentStep, mutator, sessionData)
                }
            }
            throw IllegalArgumentException("Cannot update current step as it cannot be casted to any of $classes.")
        }
    }

    private fun <UserInput, ValidationResult> updateSession(
        currentStep: Step<*, *, UserInput, ValidationResult, BaseUserInputValidator<Any?, ValidationResult, ValidationResult>>,
        mutator: (UserInput) -> UserInput,
        sessionData: FlowPayload
    ): FlowPayload {
        val newInput = mutator(currentStep.userInput)
        return if (currentStep.userInputValidator != null) {
            val validationResult = currentStep.userInputValidator.validate(currentStep.userInput, newInput, currentStep.validationResult)
            sessionData.copy(
                currentStep = currentStep.copy(
                    userInput = newInput,
                    validationResult = validationResult
                )
            )
        } else {
            sessionData.copy(
                currentStep = currentStep.copy(
                    userInput = newInput
                )
            )
        }
    }
}