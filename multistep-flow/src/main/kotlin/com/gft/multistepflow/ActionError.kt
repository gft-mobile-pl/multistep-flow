package com.gft.multistepflow

class ActionError(
    val error: Throwable,
    val action: Action<*>,
    val retryAllowed: Boolean,
    val transactionId: String
) : RuntimeException(error)