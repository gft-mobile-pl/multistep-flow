package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.utils.castOrNull

class WhenStep internal constructor(private val flow: MultiStepFlow<*>) {

    class WhenStepScope<T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> internal constructor(
        internal val flow: MultiStepFlow<*>,
        stepType: T,
        vararg stepTypes: T,
    ) {
        private val stepTypes = listOf(stepType, *stepTypes).map { type -> type::class }

        fun MultiStepFlow<in T>.updateUserInput(
            mutator: (UserInput) -> UserInput
        ) {
            UpdateUserInput(flow)<UserInput, ValidationResult>(stepTypes, mutator)
        }
    }

    operator fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit

    @JvmName("invoke_Same_Payload")
    operator fun <T : StepType<Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit

    @JvmName("invoke_Same_Payload_UserInput")
    operator fun <T : StepType<Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit

    @JvmName("invoke_Same_Payload_ValidationResult")
    operator fun <T : StepType<Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit

    @JvmName("invoke_Same_Payload_UserInput_ValidationResult")
    operator fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit

    @JvmName("invoke_Same_UserInput")
    operator fun <T : StepType<out Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit

    @JvmName("invoke_Same_UserInput_ValidationResult")
    operator fun <T : StepType<out Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit

    @JvmName("invoke_Same_ValidationResult")
    operator fun <T : StepType<out Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T,
        skipIfAnyOperationInProgress: Boolean = true,
        block: WhenStepScope<T, Payload, UserInput, ValidationResult>.(Step<T, out Payload, out UserInput, out ValidationResult, *>) -> Unit
    ) = flow.session.data.value?.run {
        if (skipIfAnyOperationInProgress && isAnyOperationInProgress) return
        currentStep.castOrNull(stepType, *stepTypes)?.let { step ->
            WhenStepScope(flow, stepType, *stepTypes).block(step)
        }
    } ?: Unit
}