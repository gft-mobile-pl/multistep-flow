package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.castOrNull

open class WhenStepUseCase(private val flow: MultiStepFlow<*>) {

    operator fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit

    @JvmName("invoke_Same_Payload")
    operator fun <T : StepType<Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit

    @JvmName("invoke_Same_Payload_UserInput")
    operator fun <T : StepType<Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit

    @JvmName("invoke_Same_Payload_ValidationResult")
    operator fun <T : StepType<Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit

    @JvmName("invoke_Same_Payload_UserInput_ValidationResult")
    operator fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit

    @JvmName("invoke_Same_UserInput")
    operator fun <T : StepType<out Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit

    @JvmName("invoke_Same_UserInput_ValidationResult")
    operator fun <T : StepType<out Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit

    @JvmName("invoke_Same_ValidationResult")
    operator fun <T : StepType<out Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: (Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let(block)
    } ?: Unit
}
