package com.gft.multistepflow.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

fun <T> CoroutineScope.asyncUndispatchedOnUnconfinedDispatcher(
    block: suspend CoroutineScope.() -> T
) = async(
    start = CoroutineStart.UNDISPATCHED,
    context = Dispatchers.Unconfined,
    block = block
)