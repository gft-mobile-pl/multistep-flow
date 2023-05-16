package com.gft.multistepflow.example.openflow.domain.validators

import com.gft.multistepflow.example.openflow.domain.model.PhoneNumber
import com.gft.multistepflow.example.openflow.domain.model.PhoneNumberValidationResult
import com.gft.multistepflow.validators.CompositeUserInputValidator

class PhoneNumberFormatValidator : CompositeUserInputValidator<PhoneNumber, PhoneNumberValidationResult>() {
    init {
        addValidator(
            validator = PrefixValidator(),
            valueProvider = { userInput.prefix },
            resultSetter = { validationResult.copy(prefixFormatValid = partialValidationResult) }
        )
        addValidator(
            validator = PhoneNumberLengthValidator(),
            valueProvider = { userInput.number },
            resultSetter = { validationResult.copy(numberFormatValid = partialValidationResult) }
        )
    }
}

private class PrefixValidator : CompositeUserInputValidator.PartialValidator<String, Boolean> {
    override fun validate(value: String): Boolean = value.startsWith("+")
        && value.length !in 2..4
        && value.substring(1).all { Character.isDigit(it) }
}

private class PhoneNumberLengthValidator : CompositeUserInputValidator.PartialValidator<String, Boolean> {
    override fun validate(value: String): Boolean = value.length in 3..12
}