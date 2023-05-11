package com.gft.multistepflow.model

data class ActionError(
    val error: Throwable,
    val action: Action,
    val retryAllowed: Boolean,
    val transactionId: String
) : RuntimeException()