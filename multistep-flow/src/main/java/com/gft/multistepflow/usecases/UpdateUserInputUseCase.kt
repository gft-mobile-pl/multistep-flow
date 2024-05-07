package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.FlowState
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.replaceLast
import com.gft.multistepflow.validators.BaseUserInputValidator

open class UpdateUserInputUseCase(
    private val flow: MultiStepFlow<*>
) {
    operator fun <Type : StepType<*, UserInput, ValidationResult, *>, UserInput, ValidationResult> invoke(
        vararg stepTypes: Type,
        mutator: (UserInput) -> UserInput
    ) {
        flow.session.update { sessionData ->
            for (stepType in stepTypes) {
                if (sessionData.currentStep.type::class == stepType::class) {
                    @Suppress("UNCHECKED_CAST")
                    val currentStep =
                        sessionData.currentStep as Step<*, *, UserInput, ValidationResult, BaseUserInputValidator<Any?, ValidationResult, ValidationResult>>
                    @Suppress("UNCHECKED_CAST")
                    return@update updateSession(currentStep, mutator, sessionData as FlowState<*, *, UserInput, *>)
                }
            }
            if (stepTypes.size == 1) throw IllegalArgumentException("Cannot update current step as its type is not  ${stepTypes.first()}.")
            else throw IllegalArgumentException("Cannot update current step as its type is not any of $stepTypes.")

        }
    }

    private fun <UserInput, ValidationResult> updateSession(
        currentStep: Step<*, *, UserInput, ValidationResult, BaseUserInputValidator<Any?, ValidationResult, ValidationResult>>,
        mutator: (UserInput) -> UserInput,
        sessionData: FlowState<*, *, UserInput, *>
    ): FlowState<*, *, *, *> {
        val newInput = mutator(currentStep.userInput)
        val updatedStep = if (currentStep.userInputValidator != null) {
            val validationResult = currentStep.userInputValidator.validate(currentStep.userInput, newInput, currentStep.validationResult)
            currentStep.copy(
                userInput = newInput,
                validationResult = validationResult
            )
        } else {
            currentStep.copy(userInput = newInput)
        }
        return sessionData.copy(
            currentStep = updatedStep,
            stepsHistory = if (flow.historyEnabled) sessionData.stepsHistory.replaceLast(updatedStep) else sessionData.stepsHistory
        )
    }
}
