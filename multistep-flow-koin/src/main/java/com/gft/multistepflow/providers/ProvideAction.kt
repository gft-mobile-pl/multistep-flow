package com.gft.multistepflow.providers

import com.gft.multistepflow.Action
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

inline fun <reified T : Action<*, *>> provideAction(): T = (object : KoinComponent {}).get()

class Actions<T> {
    inline fun <reified R : Action<*, in T>> get(): R = provideAction()
}

val <T : StepType<*, *, *, *>> Step<T, *, *, *, *>.actions
    get() = Actions<T>()
