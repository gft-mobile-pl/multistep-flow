package com.gft.multistepflow.operations

import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.UserInputValidator
import com.gft.multistepflow.utils.castOrNull
import com.gft.multistepflow.utils.replaceLast
import com.gft.observablesession.Session
import kotlin.reflect.KClass

class UpdateUserInput internal constructor(
    private val flow: MultiStepFlow<*>
) {
    internal operator fun <UserInput, ValidationResult> invoke(
        stepTypes: List<KClass<*>>,
        mutator: (UserInput) -> UserInput
    ) {
        @Suppress("UNCHECKED_CAST")
        try {
            flow.session.update { sessionData ->
                val currentStep = sessionData.currentStep
                if (currentStep.type::class !in stepTypes) {
                    if (stepTypes.size == 1) throw IllegalArgumentException("Cannot update current step - its type is not ${stepTypes.first().simpleName}.")
                    else throw IllegalArgumentException("Cannot update current step - its type is not one of: ${stepTypes.map { it.simpleName }.joinToString(", ")}.")
                }
                updateSessionData(currentStep as Step<*, *, UserInput, ValidationResult, BaseUserInputValidator<Any?, ValidationResult, ValidationResult>>, mutator, sessionData)
            }
        } catch (error: Throwable) {
            if (error is Session.SessionNotStartedException) {
                throw IllegalFlowStateException("Cannot update user input - flow $flow is not started.")
            } else {
                throw error
            }
        }
    }

    operator fun <T : StepType<*, UserInput, ValidationResult, *>, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        mutator: (UserInput) -> UserInput
    ) {
        @Suppress("UNCHECKED_CAST")
        try {
            flow.session.update { sessionData ->
                val currentStep = sessionData.currentStep
                    .castOrNull(stepType, *stepTypes) as? Step<*, *, UserInput, ValidationResult, BaseUserInputValidator<Any?, ValidationResult, ValidationResult>>
                    ?: if (stepTypes.isEmpty()) throw IllegalArgumentException("Cannot update current step - its type is not ${stepType}.")
                    else throw IllegalArgumentException("Cannot update current step - its type is not one of: ${listOf(stepType, *stepTypes).joinToString(", ")}.")
                updateSessionData(currentStep, mutator, sessionData)
            }
        } catch (error: Throwable) {
            if (error is Session.SessionNotStartedException) {
                throw IllegalFlowStateException("Cannot update user input - flow $flow is not started.")
            } else {
                throw error
            }
        }

    }

    private fun <UserInput, ValidationResult> updateSessionData(
        currentStep: Step<*, *, UserInput, ValidationResult, BaseUserInputValidator<Any?, ValidationResult, ValidationResult>>,
        mutator: (UserInput) -> UserInput,
        sessionData: FlowState<*, *, *, *>
    ): FlowState<*, *, *, *> {
        val newInput = mutator(currentStep.userInput)
        val updatedStep = if (currentStep.userInputValidator != null && currentStep.userInputValidator is UserInputValidator<UserInput, ValidationResult, *>) {
            val validationResult = currentStep.userInputValidator.internalValidate(flow, currentStep.userInput, newInput, currentStep.validationResult)
            currentStep.copyWithFlow(
                userInput = newInput,
                validationResult = validationResult
            )
        } else {
            currentStep.copyWithFlow(userInput = newInput)
        }
        return sessionData.copy(
            currentStep = updatedStep,
            stepsHistory = if (flow.historyEnabled) sessionData.stepsHistory.replaceLast(updatedStep) else sessionData.stepsHistory
        )
    }
}