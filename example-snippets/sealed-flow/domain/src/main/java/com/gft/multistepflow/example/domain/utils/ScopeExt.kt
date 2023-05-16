package com.gft.multistepflow.example.domain.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

fun CoroutineScope.launchUndispatched(block: suspend CoroutineScope.() -> Unit) = launch(start = CoroutineStart.UNDISPATCHED, block = block)