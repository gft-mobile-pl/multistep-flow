package com.gft.multistepflow.example.domain.model

import com.gft.multistepflow.example.domain.validators.PasswordFormatValidator
import com.gft.multistepflow.example.domain.validators.PhoneNumberFormatValidator
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator

/**
 * Steps
 */
sealed interface LoginStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> : StepType<Payload, UserInput, ValidationResult, Validator> {
    object CollectUsername : LoginStep<Unit, Username, Unit, DefaultNoOpValidator>
    object CollectPassword : LoginStep<Unit, Password, PasswordFormatValidationResult, PasswordFormatValidator>
    object CollectPhoneNumber : LoginStep<DisplayName, PhoneNumber, PhoneNumberValidationResult, PhoneNumberFormatValidator>
    object CollectOtp : LoginStep<DisplayName, Otp, PasswordFormatValidationResult, DefaultNoOpValidator>
}

object UnrelatedStep : StepType<String, String, Throwable?, DefaultNoOpValidator> // you can use throwable as validation result

/**
 * Payload
 */
@JvmInline
value class DisplayName(val value: String)

/**
 * User Input
 */
@JvmInline
value class Username(val value: String)

@JvmInline
value class Password(val value: String)

@JvmInline
value class Otp(val value: String)

data class PhoneNumber(
    val prefix: String,
    val number: String
)

/**
 * Validation results
 */
data class PhoneNumberValidationResult(
    val prefixFormatValid: Boolean,
    val numberFormatValid: Boolean,
) {
    val isFormatValid = prefixFormatValid && numberFormatValid
}

enum class PasswordFormatValidationResult {
    TOO_LONG,
    TOO_SHORT,
    FORMAT_VALID
}
