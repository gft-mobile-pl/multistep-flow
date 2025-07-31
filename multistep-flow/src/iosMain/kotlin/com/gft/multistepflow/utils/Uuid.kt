package com.gft.multistepflow.utils

import platform.Foundation.NSUUID

internal actual fun randomUUID(): String = NSUUID().UUIDString
