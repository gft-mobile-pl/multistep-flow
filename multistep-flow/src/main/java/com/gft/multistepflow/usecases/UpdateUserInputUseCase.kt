package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.BaseUserInputValidator
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
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

            val newInput = mutator(currentStep.userInput)
            val validationResult = if (currentStep.userInputValidator != null) {
                currentStep.userInputValidator.validate(currentStep.userInput, newInput, currentStep.validationResult)
            } else null

            sessionData.copy(
                currentStep = currentStep.copy(
                    userInput = newInput, validationResult = validationResult
                )
            )
        }
    }

    operator fun <UserInput : Any> invoke(
        vararg classes: KClass<out StepType<*, UserInput, *, *>>,
        mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->
            for (stepClass in classes) {
                if (sessionData.currentStep.type == stepClass) {
                    @Suppress("UNCHECKED_CAST")
                    val currentStep = sessionData.currentStep as Step<*, *, UserInput, Any?, BaseUserInputValidator<Any?, Any?, Any?>>
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