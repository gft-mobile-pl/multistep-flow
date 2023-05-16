package com.gft.multistepflow.example.domain.scoped.validators

import com.gft.multistepflow.example.domain.scoped.model.Password
import com.gft.multistepflow.example.domain.scoped.model.PasswordFormatValidationResult
import com.gft.multistepflow.validators.UserInputValidator

class PasswordFormatValidator : UserInputValidator<Password, PasswordFormatValidationResult> {
    override fun validate(
        currentUserInput: Password,
        newUserInput: Password,
        currentValidationResult: PasswordFormatValidationResult
    ): PasswordFormatValidationResult {
        return if (currentUserInput != newUserInput) { // optional optimization
            when {
                newUserInput.value.length < 3 -> PasswordFormatValidationResult.TOO_SHORT
                newUserInput.value.length > 8 -> PasswordFormatValidationResult.TOO_LONG
                else -> PasswordFormatValidationResult.FORMAT_VALID
            }
        } else {
            currentValidationResult
        }
    }
}