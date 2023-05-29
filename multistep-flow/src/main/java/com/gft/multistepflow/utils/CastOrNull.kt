package com.gft.multistepflow.utils

import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType

internal fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, out Payload, out UserInput, out ValidationResult, *>
} else {
    null
}

@JvmName("castOrNull_Same_Payload")
internal fun <T : StepType<Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, Payload, out UserInput, out ValidationResult, *>
} else {
    null
}

@JvmName("castOrNull_Same_Payload_UserInput")
internal fun <T : StepType<Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, Payload, UserInput, out ValidationResult, *>
} else {
    null
}

@JvmName("castOrNull_Same_Payload_ValidationResult")
internal fun <T : StepType<Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, Payload, out UserInput, ValidationResult, *>
} else {
    null
}


@JvmName("castOrNull_Same_Payload_UserInput_ValidationResult")
internal fun <T : StepType<Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, Payload, UserInput, ValidationResult, *>
} else {
    null
}

@JvmName("castOrNull_Same_UserInput")
internal fun <T : StepType<out Payload, UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, out Payload, UserInput, out ValidationResult, *>
} else {
    null
}

@JvmName("castOrNull_Same_UserInput_ValidationResult")
internal fun <T : StepType<out Payload, UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, out Payload, UserInput, ValidationResult, *>
} else {
    null
}

@JvmName("castOrNull_Same_ValidationResult")
internal fun <T : StepType<out Payload, out UserInput, ValidationResult, *>, Payload, UserInput, ValidationResult> Step<*, *, *, *, *>.castOrNull(
    stepType: T,
    vararg stepTypes: T
) = if (type == stepType || type in stepTypes) {
    @Suppress("UNCHECKED_CAST")
    this as Step<T, out Payload, out UserInput, ValidationResult, *>
} else {
    null
}