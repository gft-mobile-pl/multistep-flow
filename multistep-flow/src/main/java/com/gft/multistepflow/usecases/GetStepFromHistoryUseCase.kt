package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.castOrNull

open class GetStepFromHistoryUseCase(private val flow: MultiStepFlow<*>) {

    operator fun invoke(step: Step<*, *, *, *, *>) = flow.session.requireData().previousSteps.lastOrNull { stepFromHistory ->
        step == stepFromHistory
    } ?: throw IllegalArgumentException("Provided step was not found in flow history")

    operator fun <T : StepType<Payload, UserInput, ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, out ValidationData, *> {
        return flow.session.requireData().previousSteps.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }
}