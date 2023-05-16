package com.gft.multistepflow.example.ui

import com.gft.multistepflow.example.domain.model.LoginStep
import com.gft.multistepflow.model.Step

// step extension
fun Step<out LoginStep<*, *, *, *>, *, *, *, *>.resolveDestinationId(): Int = when(this.type) {
    LoginStep.CollectOtp -> TODO()
    LoginStep.CollectPassword -> TODO()
    LoginStep.CollectPhoneNumber -> TODO()
    LoginStep.CollectUsername -> TODO()
}

// step type extension
fun LoginStep<*, *, *, *>.resolveDestinationId(): Int = when(this) {
    LoginStep.CollectOtp -> TODO()
    LoginStep.CollectPassword -> TODO()
    LoginStep.CollectPhoneNumber -> TODO()
    LoginStep.CollectUsername -> TODO()
}