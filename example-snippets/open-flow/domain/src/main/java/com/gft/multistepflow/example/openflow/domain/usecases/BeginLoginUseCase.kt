package com.gft.multistepflow.example.openflow.domain.usecases

import com.gft.multistepflow.example.openflow.domain.model.CollectUsername
import com.gft.multistepflow.example.openflow.domain.model.UnrelatedStep
import com.gft.multistepflow.example.openflow.domain.model.Username
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.StartMultiStepFlowUseCase

class BeginLoginUseCase(
    private val startMultiStepFlow: StartMultiStepFlowUseCase<StepType<*, *, *, *>>
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