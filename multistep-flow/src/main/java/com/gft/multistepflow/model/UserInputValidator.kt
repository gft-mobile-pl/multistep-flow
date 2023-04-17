package com.gft.multistepflow.model

/**
 * Common interface of all user input validators.
 *
 * Tip: You may create generic validators factory with:
 * `inline fun <reified Validator : UserInputValidator<*, *>> provideValidator() = (object : KoinComponent {}).get<Validator>()`
 */
interface UserInputValidator<in UserInput, ValidationResult> : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>

/**
 * Do not implement this interface directly - rather use [UserInputValidator].
 * This is just an intermediate interface which simplifies generics of input validators.
 */
interface BaseUserInputValidator<in UserInput, in CurrentValidationResult, out NewValidationResult> {
    fun validate(currentUserInput: UserInput, newUserInput: UserInput, currentValidationResult: CurrentValidationResult?): NewValidationResult?
}

/**
 * A validator that does nothing and can be used with any [StepType].
 */
interface DefaultNoOpValidator : BaseUserInputValidator<Any?, Any?, Nothing>
