package com.gft.multistepflow.annotations

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Using this method in Action may lead to an undefined flow state. " +
            "If you intend to restart the flow, use MultistepFlow.resetFlow(Step) instead. " +
            "If you intend to use this method anyway, opt in to this operation to remove the error."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class FlowStartInActionScope
