package com.gft.multistepflow.annotations

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Calling MultipstepFlow<*>.end() in Action scope will likely lead to a deadlock. " +
            "Consider using MultistepFlow<*>.endImmediately() instead. " +
            "If you intend to end a different flow, opt in to this operation to remove the error."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class FlowEndInActionScope()
