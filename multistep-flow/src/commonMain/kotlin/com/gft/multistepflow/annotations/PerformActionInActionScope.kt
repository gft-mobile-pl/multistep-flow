package com.gft.multistepflow.annotations

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Calling Step.performAction(Action) within another Action currently being performed in the scope of a flow to which the Step belongs is not allowed. " +
            "If you need to launch the action immediately, use Step.performChildAction(Action) instead. " +
            "Alternatively, to enqueue the action, explicitly opt in to Step.performAction(Action) and ensure it is invoked in a parallel scope, such as GlobalScope."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class PerformActionInActionScope