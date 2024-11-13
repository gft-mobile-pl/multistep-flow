package com.gft.multistepflow.annotations

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Restarting the flow in Action may lead to an undefined flow state. " +
            "Use MultistepFlow.resetFlow(Step) instead. " +
            "If you intend to start a different flow, opt in to this operation to remove the error."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class FlowStartInActionScope
