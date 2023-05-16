package com.gft.multistepflow.example.domain.utils

import com.gft.multistepflow.model.Action
import com.gft.multistepflow.validators.BaseUserInputValidator
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

inline fun <reified T : Action> provideAction(): T = (object : KoinComponent {}).get()

inline fun <reified T : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>, UserInput, ValidationResult> provideValidator(): T = (object : KoinComponent {}).get()