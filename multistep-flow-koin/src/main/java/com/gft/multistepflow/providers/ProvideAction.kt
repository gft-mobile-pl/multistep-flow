package com.gft.multistepflow.providers

import com.gft.multistepflow.model.Action
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

inline fun <reified T : Action> provideAction(): T = (object : KoinComponent {}).get()