package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.filterByStepType
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

open class AwaitStepUseCase(private val flow: MultiStepFlow<*>) {

    suspend operator fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, out ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }

    @JvmName("invoke_Same_Payload")
    suspend operator fun <T : StepType<Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, out UserInput, out ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }

    @JvmName("invoke_Same_Payload_UserInput")
    suspend operator fun <T : StepType<Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, UserInput, out ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }

    @JvmName("invoke_Same_Payload_ValidationResult")
    suspend operator fun <T : StepType<Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, out UserInput, ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }

    @JvmName("invoke_Same_Payload_UserInput_ValidationResult")
    suspend operator fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, UserInput, ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }

    @JvmName("invoke_Same_UserInput")
    suspend operator fun <T : StepType<out Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, UserInput, out ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }

    @JvmName("invoke_Same_UserInput_ValidationResult")
    suspend operator fun <T : StepType<out Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, UserInput, ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }

    @JvmName("invoke_Same_ValidationResult")
    suspend operator fun <T : StepType<out Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, ValidationResult, *> {
        return flow.session.data
            .filterNotNull()
            .map { flowData -> flowData.currentStep }
            .filterByStepType(stepType, *stepTypes)
            .first()
    }
}