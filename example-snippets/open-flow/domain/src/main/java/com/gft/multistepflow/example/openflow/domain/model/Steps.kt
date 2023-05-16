package com.gft.multistepflow.example.openflow.domain.model

import com.gft.multistepflow.example.openflow.domain.validators.PasswordFormatValidator
import com.gft.multistepflow.example.openflow.domain.validators.PhoneNumberFormatValidator
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.validators.DefaultNoOpValidator

/**
 * Steps
 */
object CollectUsername: StepType<Unit, Username, Unit, DefaultNoOpValidator>
object CollectPassword: StepType<Unit, Password, PasswordFormatValidationResult, PasswordFormatValidator>
object CollectPhoneNumber : StepType<DisplayName, PhoneNumber, PhoneNumberValidationResult, PhoneNumberFormatValidator> // multifield validation; btw: if you want to pass prefixes list do not put it as step's payload!
object CollectOtp : StepType<DisplayName, Otp, PasswordFormatValidationResult, DefaultNoOpValidator>

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




