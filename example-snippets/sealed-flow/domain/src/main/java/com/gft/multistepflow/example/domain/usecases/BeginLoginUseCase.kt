package com.gft.multistepflow.example.domain.usecases

import com.gft.multistepflow.example.domain.model.LoginStep
import com.gft.multistepflow.example.domain.model.Username
import com.gft.multistepflow.model.Step

class BeginLoginUseCase internal constructor(
    private val startLoginFlow: StartLoginFlowUseCase
) {
    suspend operator fun invoke() {
        startLoginFlow(
            currentStep = Step(
                type = LoginStep.CollectUsername,
                payload = Unit,
                userInput = Username("")
            )
        )

        // Full step type safety! :)
        // This code won't compile
        // startLoginFlow(
        //     currentStep = Step(
        //         type = UnrelatedStep,
        //         payload = "",
        //         userInput = "",
        //         validationResult = null
        //     )
        // )
    }
}