package com.gft.multistepflow.validators

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
sealed interface BaseUserInputValidator<in UserInput, in CurrentValidationResult, out NewValidationResult> {
    fun validate(currentUserInput: UserInput, newUserInput: UserInput, currentValidationResult: CurrentValidationResult): NewValidationResult
}

/**
 * A validator that does nothing and can be used with any [com.gft.multistepflow.model.StepType].
 */
interface DefaultNoOpValidator : BaseUserInputValidator<Any?, Any?, Nothing>

/**
 * A validator that uses a composite pattern to delegate field validation to designated [PartialValidator] instances.
 */
abstract class CompositeUserInputValidator<UserInput, ValidationResult> : UserInputValidator<UserInput, ValidationResult> {
    @PublishedApi
    internal val validators: MutableList<ValidatorsListItem<*, *>> = mutableListOf()

    protected fun <Value, PartialValidationResult> addValidator(
        validator: PartialValidator<Value, PartialValidationResult>,
        valueProvider: ValueProviderContext.() -> Value,
        resultSetter: ResultSetterContext<PartialValidationResult>.() -> ValidationResult,
    ) {
        validators.add(ValidatorsListItem(validator, valueProvider, resultSetter))
    }

    inner class ValidatorsListItem<Value, PartialValidationResult>(
        val validator: PartialValidator<Value, PartialValidationResult>,
        val valueProvider: ValueProviderContext.() -> Value,
        val partialResultSetter: ResultSetterContext<PartialValidationResult>.() -> ValidationResult
    )

    @Suppress("UNCHECKED_CAST")
    override fun validate(currentUserInput: UserInput, newUserInput: UserInput, currentValidationResult: ValidationResult): ValidationResult {
        return validators.fold(currentValidationResult) { newUserInputValidationResult, item ->
            val currentValue = item.valueProvider(ValueProviderContext(currentUserInput))
            val newValue = item.valueProvider(ValueProviderContext(newUserInput))
            if (currentValue != newValue) {
                val fieldValidationResult = (item.validator as PartialValidator<Any?, *>).validate(newValue)
                (item.partialResultSetter as ResultSetterContext<*>.() -> ValidationResult)(ResultSetterContext(newUserInputValidationResult, fieldValidationResult))
            } else {
                newUserInputValidationResult
            }
        }
    }

    inner class ValueProviderContext(
        val userInput: UserInput
    )

    inner class ResultSetterContext<PartialValidationResult>(
        val validationResult: ValidationResult,
        val partialValidationResult: PartialValidationResult
    )

    interface PartialValidator<in Value, PartialValidationResult> {
        fun validate(value: Value): PartialValidationResult
    }
}
