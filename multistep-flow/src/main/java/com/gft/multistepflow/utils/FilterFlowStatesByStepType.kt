package com.gft.multistepflow.utils

import com.gft.multistepflow.model.FlowState
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

@JvmName("filterFlowStatesByStepType")
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

@JvmName("filterFlowStatesByStepType_Same_Payload")
fun <T : StepType<Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, Payload, out UserInput, out ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, Payload, out UserInput, out ValidationResult>
    } else {
        null
    }
}

@JvmName("filterFlowStatesByStepType_Same_Payload_UserInput")
fun <T : StepType<Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, Payload, UserInput, out ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, Payload, UserInput, out ValidationResult>
    } else {
        null
    }
}

@JvmName("filterFlowStatesByStepType_Same_Payload_ValidationResult")
fun <T : StepType<Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, Payload, out UserInput, ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, Payload, out UserInput, ValidationResult>
    } else {
        null
    }
}

@JvmName("filterFlowStatesByStepType_Same_Payload_UserInput_ValidationResult")
fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, Payload, UserInput, ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, Payload, UserInput, ValidationResult>
    } else {
        null
    }
}

@JvmName("filterFlowStatesByStepType_Same_UserInput")
fun <T : StepType<out Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, out Payload, UserInput, out ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, out Payload, UserInput, out ValidationResult>
    } else {
        null
    }
}

@JvmName("filterFlowStatesByStepType_Same_UserInput_ValidationResult")
fun <T : StepType<out Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, out Payload, UserInput, ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, out Payload, UserInput, ValidationResult>
    } else {
        null
    }
}

@JvmName("filterFlowStatesByStepType_Same_ValidationResult")
fun <T : StepType<out Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<FlowState<*, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<FlowState<T, out Payload, out UserInput, ValidationResult>> = mapNotNull { flowState ->
    if (flowState.containsStepOfType(stepType, *stepTypes)) {
        @Suppress("UNCHECKED_CAST")
        flowState as FlowState<T, out Payload, out UserInput, ValidationResult>
    } else {
        null
    }
}

private fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> FlowState<*, *, *, *>.containsStepOfType(
    stepType: T,
    vararg stepTypes: T
): Boolean = currentStep.type == stepType || currentStep.type in stepTypes

