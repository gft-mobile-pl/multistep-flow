package com.gft.multistepflow.annotations

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Read before use: ensure MultiStepFlow.clear() is the last statement in the Action body, as any subsequent code will not be executed! " +
            "If you intend to restart the flow, use MultiStepFlow.restart(Step) instead. Opt in to this method to remove the warning."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class ClearFlowInActionScope
