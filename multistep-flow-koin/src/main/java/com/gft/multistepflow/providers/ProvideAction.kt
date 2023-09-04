package com.gft.multistepflow.providers

import com.gft.multistepflow.model.Action
import com.gft.multistepflow.model.ParametrizedAction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

inline fun <reified T : Action> provideAction(): T = (object : KoinComponent {}).get()

inline fun <reified T : ParametrizedAction<R>, R> provideAction(argument: R): T = (object : KoinComponent {}).get(
    parameters = { parametersOf(argument) }
)
