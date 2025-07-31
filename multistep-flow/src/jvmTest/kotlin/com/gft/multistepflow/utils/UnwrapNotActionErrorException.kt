package com.gft.multistepflow.utils

import com.gft.multistepflow.NotActionErrorException

fun Throwable.unwrapNotActionErrorException() = if (this is NotActionErrorException) this.error else this