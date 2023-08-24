package com.gft.multistepflow.model

import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import com.gft.multistepflow.validators.UserInputValidator

interface StepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>>

class Step<Type : StepType<Payload, UserInput, ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> private constructor(
    val type: Type,
    val payload: Payload,
    val userInput: UserInput,
    val validationResult: ValidationResult,
    internal val userInputValidator: Validator? = null,
    val error: ActionError? = null
) {
    val actions: Actions<Type> = Actions()

    // Suppressing warning for unused generic type - it's not used here inside of the class, but it's used to ensure type safety when getting actions from steps
    @Suppress("unused")
    class Actions<StepType> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    fun copy(
        payload: Payload = this.payload,
        userInput: UserInput = this.userInput,
        validationResult: ValidationResult = this.validationResult,
        error: ActionError? = this.error
    ): Step<Type, Payload, UserInput, ValidationResult, Validator> = Step(
        type = type,
        payload = payload,
        userInput = userInput,
        validationResult = validationResult,
        userInputValidator = userInputValidator,
        error = error
    )

    override fun toString(): String {
        return "Step(type=$type, payload=$payload, userInput=$userInput, validationResult=$validationResult, userInputValidator=$userInputValidator, actions=$actions, error=$error)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Step<*, *, *, *, *>

        if (type != other.type) return false
        if (payload != other.payload) return false
        if (userInput != other.userInput) return false
        if (validationResult != other.validationResult) return false
        if (userInputValidator != other.userInputValidator) return false
        if (error != other.error) return false
        if (actions != other.actions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (payload?.hashCode() ?: 0)
        result = 31 * result + (userInput?.hashCode() ?: 0)
        result = 31 * result + (validationResult?.hashCode() ?: 0)
        result = 31 * result + (userInputValidator?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + actions.hashCode()
        return result
    }

    companion object {
        // all fields required
        operator fun <Type : StepType<Payload, UserInput, ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : UserInputValidator<UserInput, ValidationResult>> invoke(
            type: Type,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult,
            validator: Validator
        ) = Step(type, payload, userInput, validationResult, validator)

        // validator not required
        operator fun <Type : StepType<Payload, UserInput, ValidationResult, DefaultNoOpValidator>, Payload, UserInput, ValidationResult> invoke(
            type: Type,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult
        ) = Step(type, payload, userInput, validationResult, null)

        // validator and validation result not required
        operator fun <Type : StepType<Payload, UserInput, Unit, DefaultNoOpValidator>, Payload, UserInput> invoke(
            type: Type,
            payload: Payload,
            userInput: UserInput
        ) = Step(type, payload, userInput, Unit, null)

        operator fun <Type : StepType<Unit, Unit, Unit, DefaultNoOpValidator>> invoke(type: Type) = Step(type, Unit, Unit, Unit)
    }
}
