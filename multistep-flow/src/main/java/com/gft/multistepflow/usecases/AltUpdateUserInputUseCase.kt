package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.AltMultiStepFlow
import com.gft.multistepflow.model.AltStep
import com.gft.multistepflow.model.AltStepType
import kotlin.reflect.KClass

abstract class AltUpdateUserInputUseCase(
    private val flow: AltMultiStepFlow
) {
    inline operator fun <reified StepType : AltStepType<*, UserInput, *, *>, reified UserInput : Any> invoke(
        noinline mutator: (UserInput) -> UserInput
    ) {
        //this(StepType::class, mutator) // << działa, bo wskazuje na Klasę a nie konkretną przeciążoną metodę!
        this.invoke(StepType::class, mutator) // << wymaga PublishedApi, bo wskazuje na ukrytą metodę!
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal operator fun <Type : AltStepType<*, UserInput, *, ValidationResult>, UserInput, ValidationResult> invoke(
        stepClass: KClass<Type>,
        mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->

            if (sessionData.currentStep.type != stepClass) {
                throw IllegalArgumentException("Cannot update current step as it cannot be casted to $stepClass")
            }
            val currentStep = sessionData.currentStep as AltStep<*, *, UserInput, ValidationResult, *>

            val newInput = mutator(currentStep.userInput)
            val validationResult = if (currentStep.userInputValidator != null) {
                currentStep.userInputValidator.validate(currentStep.userInput, newInput, currentStep.validationResult)
            } else null

            sessionData.copy(
                currentStep = currentStep.copy(
                    userInput = newInput,
                    validationResult = validationResult
                )
            )
        }
    }

    operator fun <UserInput : Any> invoke(
        vararg classes: KClass<out AltStepType<*, UserInput, *, *>>,
        mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->
            for (stepClass in classes) {
                if (sessionData.currentStep.type == stepClass) {
                    @Suppress("UNCHECKED_CAST")
                    val currentStep = sessionData.currentStep as AltStep<*, *, UserInput, Any?, *>
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