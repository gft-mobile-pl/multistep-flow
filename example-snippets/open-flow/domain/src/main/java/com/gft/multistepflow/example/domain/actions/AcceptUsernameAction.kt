package com.gft.multistepflow.example.domain.actions

import com.gft.multistepflow.example.domain.model.CollectPassword
import com.gft.multistepflow.example.domain.model.CollectUsername
import com.gft.multistepflow.example.domain.model.Password
import com.gft.multistepflow.example.domain.model.PasswordFormatValidationResult
import com.gft.multistepflow.example.domain.model.UnrelatedStep
import com.gft.multistepflow.model.Action
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.providers.provideAction
import com.gft.multistepflow.providers.provideValidator
import com.gft.multistepflow.usecases.RequireStepUseCase
import com.gft.multistepflow.usecases.SetStepUseCase

class AcceptUsernameAction(
    private val requireStepUseCase: RequireStepUseCase,
    private val setStep: SetStepUseCase<StepType<*, *, *, *>>
) : Action() {
    override suspend fun perform(transactionId: String) {
        requireStepUseCase(CollectUsername)

        setStep(
            Step(
                type = CollectPassword,
                payload = Unit,
                userInput = Password(""),
                validationResult = PasswordFormatValidationResult.TOO_SHORT,
                validator = provideValidator()
            )
        )

        // Warning!
        // Lack of step type safety - you may provide unrelated step
        setStep(
            Step(
                type = UnrelatedStep,
                payload = "",
                userInput = "",
                validationResult = null
            )
        )
    }
}

fun Step.Actions<CollectUsername>.getAcceptUsernameAction() = provideAction<AcceptUsernameAction>()