package com.gft.multistepflow.model

import kotlin.reflect.KClass

interface AltStepType<Payload, UserInput, ValidationResult, in Validator : UserInputValidator<UserInput, ValidationResult>>

class AltStep<Type : AltStepType<Payload, UserInput, ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : UserInputValidator<UserInput, ValidationResult>> private constructor(
    val type: KClass<Type>,
    val payload: Payload,
    val userInput: UserInput,
    val validationResult: ValidationResult? = null,
    val userInputValidator: UserInputValidator<UserInput, ValidationResult>? = null
) {
    val actions: Actions<Type> = Actions()

    // Suppressing warning for unused generic type - it's not used here inside of the class, but it's used to ensure type safety when getting actions from steps
    @Suppress("unused")
    class Actions<StepType>

    fun copy(
        payload: Payload = this.payload,
        userInput: UserInput = this.userInput,
        validationResult: ValidationResult? = this.validationResult,
    ): AltStep<Type, Payload, UserInput, ValidationResult, Validator> = AltStep(
        type = type,
        payload = payload,
        userInput = userInput,
        validationResult = validationResult,
        userInputValidator = userInputValidator
    )

    override fun toString(): String {
        return "AltStep(type=$type, payload=$payload, userInput=$userInput, validationResult=$validationResult, userInputValidator=$userInputValidator, actions=$actions)"
    }

    companion object {
        // all fields required
        operator fun  <Type : AltStepType<Payload, UserInput, ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : UserInputValidator<UserInput, ValidationResult>> invoke(
            type: KClass<Type>,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult? = null,
            validator: Validator
        ) = AltStep(type, payload, userInput, validationResult, validator)

        // validator not required
        operator fun  <Type : AltStepType<Payload, UserInput, ValidationResult, NoOpValidator>, Payload, UserInput, ValidationResult> invoke(
            type: KClass<Type>,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult? = null,
        ) = AltStep(type, payload, userInput, validationResult, null)

        // payload not required
        operator fun  <Type : AltStepType<Unit, UserInput, ValidationResult, Validator>, UserInput, ValidationResult, Validator : UserInputValidator<UserInput, ValidationResult>> invoke(
            type: KClass<Type>,
            userInput: UserInput,
            validationResult: ValidationResult? = null,
            validator: Validator
        ) = AltStep(type, Unit, userInput, validationResult, validator)

        // payload not required; validator not required
        operator fun  <Type : AltStepType<Unit, UserInput, ValidationResult, NoOpValidator>, UserInput, ValidationResult> invoke(
            type: KClass<Type>,
            userInput: UserInput,
            validationResult: ValidationResult? = null,
        ) = AltStep(type, Unit, userInput, validationResult, null)

        // payload not required; validation result not required -> validator not required
        operator fun  <Type : AltStepType<Unit, UserInput, Unit, NoOpValidator>, UserInput> invoke(
            type: KClass<Type>,
            userInput: UserInput
        ) = AltStep(type, Unit, userInput, Unit, null)

        // input not required -> validation result not required -> validator not required
        operator fun  <Type : AltStepType<Payload, Unit, Unit, NoOpValidator>, Payload> invoke(
            type: KClass<Type>,
            payload: Payload
        ) = AltStep(type, payload, Unit, Unit, null)

        // validation result not required -> validator not required
        operator fun  <Type : AltStepType<Payload, UserInput, Unit, NoOpValidator>, Payload, UserInput> invoke(
            type: KClass<Type>,
            payload: Payload,
            userInput: UserInput,
        ) = AltStep(type, payload, userInput, null, null)

        // nothing required
        operator fun  <Type : AltStepType<Unit, Unit, Unit, NoOpValidator>> invoke(
            type: KClass<Type>,
        ) = AltStep(type, Unit, Unit, Unit, null)
    }
}

















