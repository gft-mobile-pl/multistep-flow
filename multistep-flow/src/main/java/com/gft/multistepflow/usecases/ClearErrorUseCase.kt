package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow

open class ClearErrorUseCase(
    private val flow: MultiStepFlow<*>
) {
    operator fun invoke() {
        flow.session.update { flowState ->
            flowState.copy(
                currentStep = flowState.currentStep.copy(
                    error = null
                )
            )
        }
    }
}
