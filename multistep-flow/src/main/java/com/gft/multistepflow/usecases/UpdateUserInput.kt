package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import kotlin.reflect.KClass

abstract class UpdateUserInputUseCase(
    private val flow: MultiStepFlow
) {
    inline operator fun <reified StepType : Step<StepType, *, UserInput, ValidationResult>, reified UserInput : Any, ValidationResult> invoke(
        noinline mutator: (UserInput) -> UserInput
    ) {
        this(StepType::class, mutator)
    }

    private operator fun <StepType : Step<StepType, *, UserInput, ValidationResult>, UserInput : Any, ValidationResult> invoke(
        stepClass: KClass<StepType>,
        mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->
            if (!stepClass.isInstance(sessionData.currentStep)) {
                throw IllegalArgumentException("Cannot update current step as it cannot be casted to $stepClass")
            }

            @Suppress("UNCHECKED_CAST")
            val currentStep = sessionData.currentStep as StepType
            val newInput = mutator(currentStep.userInput)
            val validationResult = if (currentStep.userInputValidator != null) {
                currentStep.userInputValidator.validate(currentStep.userInput, newInput, currentStep.validationResult)
            } else null

            @Suppress("UNCHECKED_CAST")
            sessionData.copy(
                currentStep = (currentStep as Step<*, *, UserInput, Any?>).copy(
                    userInput = newInput,
                    validationResult = validationResult
                )
            )
        }
    }

    operator fun <UserInput : Any, ValidationResult> invoke(
        vararg classes: KClass<out Step<*, *, UserInput, ValidationResult>>,
        mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->
            for (stepClass in classes) {
                if (stepClass.isInstance(sessionData.currentStep)) {
                    @Suppress("UNCHECKED_CAST")
                    val currentStep = sessionData.currentStep as Step<*, *, UserInput, ValidationResult>
                    val newInput = mutator(currentStep.userInput)
                    val validationResult = if (currentStep.userInputValidator != null) {
                        currentStep.userInputValidator.validate(currentStep.userInput, newInput, currentStep.validationResult)
                    } else null
                    return@update sessionData.copy(
                        currentStep = currentStep.copy(
                            userInput = newInput,
                            validationResult = validationResult
                        )
                    )
                }
            }
            throw IllegalArgumentException("Cannot update current step as it cannot be casted to any of $classes.")
        }
    }
}