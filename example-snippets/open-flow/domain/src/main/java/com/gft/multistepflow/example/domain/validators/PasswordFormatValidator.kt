package com.gft.multistepflow.example.domain.validators

import com.gft.multistepflow.example.domain.model.Password
import com.gft.multistepflow.example.domain.model.PasswordFormatValidationResult
import com.gft.multistepflow.example.domain.model.PasswordFormatValidationResult.FORMAT_VALID
import com.gft.multistepflow.example.domain.model.PasswordFormatValidationResult.TOO_LONG
import com.gft.multistepflow.example.domain.model.PasswordFormatValidationResult.TOO_SHORT
import com.gft.multistepflow.validators.UserInputValidator

class PasswordFormatValidator : UserInputValidator<Password, PasswordFormatValidationResult> {
    override fun validate(
        currentUserInput: Password,
        newUserInput: Password,
        currentValidationResult: PasswordFormatValidationResult
    ): PasswordFormatValidationResult {
        return if (currentUserInput != newUserInput) { // optional optimization
            when {
                newUserInput.value.length < 3 -> TOO_SHORT
                newUserInput.value.length > 8 -> TOO_LONG
                else -> FORMAT_VALID
            }
        } else {
            currentValidationResult
        }
    }
}