package com.gft.multistepflow.model

class ActionError(
    val error: Throwable,
    val action: Action,
    val retryAllowed: Boolean,
    val transactionId: String
) : RuntimeException(error)