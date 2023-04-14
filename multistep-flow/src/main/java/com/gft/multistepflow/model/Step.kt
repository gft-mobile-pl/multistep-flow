package com.gft.multistepflow.model

abstract class Step<StepType : Step<StepType, Payload, UserInput, ValidationResult>, Payload, UserInput, ValidationResult> constructor(
    val payload: Payload,
    val userInput: UserInput,
    val validationResult: ValidationResult? = null,
    val userInputValidator: UserInputValidator<UserInput, ValidationResult>? = null,
) {
    val actions: Actions<StepType> = Actions()

    // Suppressing warning for unused generic type - it's not used here inside of the class, but it's used to ensure type safety when getting actions from steps
    @Suppress("unused")
    class Actions<StepType>

    abstract fun copy(
        payload: Payload = this.payload,
        userInput: UserInput = this.userInput,
        validationResult: ValidationResult? = this.validationResult,
    ): StepType
}