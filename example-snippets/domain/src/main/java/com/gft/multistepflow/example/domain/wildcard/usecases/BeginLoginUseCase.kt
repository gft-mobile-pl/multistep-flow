package com.gft.multistepflow.example.domain.wildcard.usecases

import com.gft.multistepflow.example.domain.wildcard.model.CollectUsername
import com.gft.multistepflow.example.domain.wildcard.model.UnrelatedStep
import com.gft.multistepflow.example.domain.wildcard.model.Username
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.StartMultiStepFlow

class BeginLoginUseCase(
    private val startMultiStepFlow: StartMultiStepFlow<StepType<*, *, *, *>>
) {
    suspend operator fun invoke() {
        startMultiStepFlow(
            currentStep = Step(
                type = CollectUsername,
                payload = Unit,
                userInput = Username("")
            )
        )

        // Warning!
        // Lack of step type safety - you may provide unrelated step while starting the flow!
        startMultiStepFlow(
            currentStep = Step(
                type = UnrelatedStep,
                payload = "",
                userInput = "",
                validationResult = null
            )
        )
    }
}