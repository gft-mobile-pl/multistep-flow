package com.gft.multistepflow.example.domain.actions

import com.gft.multistepflow.example.domain.model.LoginStep.CollectPassword
import com.gft.multistepflow.example.domain.model.LoginStep.CollectUsername
import com.gft.multistepflow.example.domain.model.Password
import com.gft.multistepflow.example.domain.model.PasswordFormatValidationResult
import com.gft.multistepflow.example.domain.usecases.RequireLoginStepUseCase
import com.gft.multistepflow.example.domain.usecases.SetLoginStepUseCase
import com.gft.multistepflow.model.Action
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.providers.provideAction
import com.gft.multistepflow.providers.provideValidator

class AcceptUsernameAction(
    private val requireStepUseCase: RequireLoginStepUseCase,
    private val setStep: SetLoginStepUseCase
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

        // Full step type safety!
        // The code below won't compile
        // setStep(
        //     Step(
        //         type = UnrelatedStep,
        //         payload = "",
        //         userInput = "",
        //         validationResult = null
        //     )
        // )
    }
}

fun Step.Actions<CollectUsername>.getAcceptUsernameAction() = provideAction<AcceptUsernameAction>()