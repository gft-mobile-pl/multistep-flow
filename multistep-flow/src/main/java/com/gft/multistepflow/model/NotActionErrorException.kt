package com.gft.multistepflow.model

class NotActionErrorException(
    error: Throwable,
    action: Action
) : RuntimeException(
    "During the execution of the \"$action\" action, a \"$error\" exception was thrown. However, this exception does not conform to the \"com.gft.multistepflow.model.ActionError\" standard required by our multistep flow. To maintain uniformity in error handling, ensure that any exceptions thrown within actions are encapsulated using the \"com.gft.multistepflow.model.ActionError\" class.",
    error
)
