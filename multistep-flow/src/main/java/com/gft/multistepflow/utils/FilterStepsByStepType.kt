package com.gft.multistepflow.utils

import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

@JvmName("filterStepsFlowByStepType")
fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, out Payload, out UserInput, out ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterStepsFlowByStepType_Same_Payload")
fun <T : StepType<Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, Payload, out UserInput, out ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterStepsFlowByStepType_Same_Payload_UserInput")
fun <T : StepType<Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, Payload, UserInput, out ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterStepsFlowByStepType_Same_Payload_ValidationResult")
fun <T : StepType<Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, Payload, out UserInput, ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterStepsFlowByStepType_Same_Payload_UserInput_ValidationResult")
fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, Payload, UserInput, ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterStepsFlowByStepType_Same_UserInput")
fun <T : StepType<out Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, out Payload, UserInput, out ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterStepsFlowByStepType_Same_UserInput_ValidationResult")
fun <T : StepType<out Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, out Payload, UserInput, ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}

@JvmName("filterStepsFlowByStepType_Same_ValidationResult")
fun <T : StepType<out Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Flow<Step<*, *, *, *, *>>.filterByStepType(
    stepType: T,
    vararg stepTypes: T
): Flow<Step<T, out Payload, out UserInput, ValidationResult, *>> = mapNotNull { step ->
    step.castOrNull(stepType, *stepTypes)
}