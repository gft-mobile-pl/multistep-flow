package com.gft.multistepflow

import com.gft.multistepflow.operations.PerformAction

interface StepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>>

class Step<Type : StepType<Payload, UserInput, ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> private constructor(
    val type: Type,
    val payload: Payload,
    val userInput: UserInput,
    val validationResult: ValidationResult,
    internal val userInputValidator: Validator? = null,
) {
    internal var flow: MultiStepFlow<in Type>? = null

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
    ): Step<Type, Payload, UserInput, ValidationResult, Validator> = Step(
        type = type,
        payload = payload,
        userInput = userInput,
        validationResult = validationResult,
        userInputValidator = userInputValidator,
    )

    fun copyWithFlow(
        payload: Payload = this.payload,
        userInput: UserInput = this.userInput,
        validationResult: ValidationResult = this.validationResult,
    ): Step<Type, Payload, UserInput, ValidationResult, Validator> = Step(
        type = type,
        payload = payload,
        userInput = userInput,
        validationResult = validationResult,
        userInputValidator = userInputValidator,
    ).also { step -> step.flow = flow }

    override fun toString(): String {
        return "Step(" +
                "type=${type::class.simpleName}, " +
                "payload=$payload, " +
                "userInput=$userInput, " +
                "validationResult=$validationResult, " +
                "userInputValidator=${userInputValidator?.let { validator -> validator::class.simpleName }}, " +
                ")"
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

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (payload?.hashCode() ?: 0)
        result = 31 * result + (userInput?.hashCode() ?: 0)
        result = 31 * result + (validationResult?.hashCode() ?: 0)
        result = 31 * result + (userInputValidator?.hashCode() ?: 0)
        return result
    }

    companion object {
        // all fields required
        operator fun <Type : StepType<Payload, UserInput, ValidationResult, Validator>, Payload, UserInput, ValidationResult, Validator : UserInputValidator<UserInput, ValidationResult>> invoke(
            type: Type,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult,
            validator: Validator,
        ) = Step(type, payload, userInput, validationResult, validator)

        // validator not required
        operator fun <Type : StepType<Payload, UserInput, ValidationResult, DefaultNoOpValidator>, Payload, UserInput, ValidationResult> invoke(
            type: Type,
            payload: Payload,
            userInput: UserInput,
            validationResult: ValidationResult,
        ) = Step(type, payload, userInput, validationResult, null)

        // validator and validation result not required
        operator fun <Type : StepType<Payload, UserInput, Unit, DefaultNoOpValidator>, Payload, UserInput> invoke(
            type: Type,
            payload: Payload,
            userInput: UserInput,
        ) = Step(type, payload, userInput, Unit, null)

        operator fun <Type : StepType<Unit, Unit, Unit, DefaultNoOpValidator>> invoke(type: Type) = Step(type, Unit, Unit, Unit)
    }
}

val <Type : StepType<*, *, *, *>> Step<Type, *, *, *, *>.performAction: PerformAction<Type>
    get() = PerformAction(flow)
