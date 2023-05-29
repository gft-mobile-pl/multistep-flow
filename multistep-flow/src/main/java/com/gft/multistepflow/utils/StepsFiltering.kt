package com.gft.multistepflow.utils

import com.gft.multistepflow.model.FlowState
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, out Payload, out UserInput, out ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterFlowStateFlowByStepType")
fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, out Payload, out UserInput, out ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, out Payload, out UserInput, out ValidationResult>
    } else {
        null
    }
}

private fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> FlowState<*, *, *, *>.containsStepOfType(
    stepType: T,
    vararg stepTypes: T
): Boolean = currentStep.type == stepType || currentStep.type in stepTypes

