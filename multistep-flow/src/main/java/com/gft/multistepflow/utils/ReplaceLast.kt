package com.gft.multistepflow.utils

internal fun <T> List<T>.replaceLast(item: T) = slice(0 until lastIndex) + item