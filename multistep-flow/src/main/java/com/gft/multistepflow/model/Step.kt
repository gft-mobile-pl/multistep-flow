package com.gft.multistepflow.model

import kotlin.reflect.KClass

interface StepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>>

class Step<Type : StepType<Payload, UserInput, out ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> private constructor(
    val type: KClass<Type>,
    val payload: Payload,
    val userInput: UserInput,
    val validationResult: ValidationResult? = null,
    internal val userInputValidator: Validator? = null
) {
    val actions: Actions<Type> = Actions()

    // Suppressing warning for unused generic type - it's not used here inside of the class, but it's used to ensure type safety when getting actions from steps
    @Suppress("unused")
    class Actions<StepType>

    fun copy(
        payload: Payload = this.payload,
        userInput: UserInput = this.userInput,
        validationResult: ValidationResult? = this.validationResult,
    ): Step<Type, Payload, UserInput, ValidationResult, Validator> = Step(
        type = type,
        payload = payload,
        userInput = userInput,
        validationResult = validationResult,
        userInputValidator = userInputValidator
    )

    override fun toString(): String {
        return "Step(type=$type, payload=$payload, userInput=$userInput, validationResult=$validationResult, userInputValidator=$userInputValidator, actions=$actions)"
    }

    companion object {
        // all fields required
        operator fun <Type : StepType<Payload, UserInput, ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : UserInputValidator<UserInput, ValidationResult>> invoke(
            type: KClass<Type>,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult? = null,
            validator: Validator
        ) = Step(type, payload, userInput, validationResult, validator)

        // validator not required
        operator fun <Type : StepType<Payload, UserInput, ValidationResult, DefaultNoOpValidator>, Payload, UserInput, ValidationResult> invoke(
            type: KClass<Type>,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult? = null,
        ) = Step(type, payload, userInput, validationResult, null)
    }
}

















