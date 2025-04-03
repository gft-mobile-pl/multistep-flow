package com.gft.multistepflow

import kotlinx.coroutines.CancellationException

class ActionError(
    val error: Throwable,
    val action: Action<*, *>,
    val retryAllowed: Boolean,
    val transactionId: String
) : RuntimeException(error) {
    init {
        if (error is CancellationException) throw error
    }
}