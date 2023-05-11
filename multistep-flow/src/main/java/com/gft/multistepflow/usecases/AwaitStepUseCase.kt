package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.filterByStepType
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

open class AwaitStepUseCase(private val flow: MultiStepFlow<*>) {

    suspend operator fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, out ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }
}