package com.gft.multistepflow.model

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KClass

interface UserInputValidator<in UserInput, in ValidationResult> {
    fun <T : ValidationResult> validate(oldUserInput: UserInput, newUserInput: UserInput, oldValidationResult: ValidationResult?): T?
}

// mogę użyć Nothing jeśli to jest >>out<< ValidationResult. Nothing dziedziczy po wszystkim na raz.
interface NoOpValidator : UserInputValidator<Any?, Any?>

inline fun <reified StepType : AltStepType<*, *, *, Validator>, reified Validator : UserInputValidator<*, *>> provideValidator(
    stepType: KClass<StepType>
) = (object : KoinComponent {}).get<Validator>()
