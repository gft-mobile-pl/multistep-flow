package com.gft.multistepflow

abstract class Action {
    protected abstract suspend fun ActionScope.perform(transactionId: String)

    internal suspend fun internalPerform(transactionId: String) = ActionScopeImplementation().perform(transactionId)

    override fun toString(): String = this::class.simpleName ?: super.toString()

    interface ActionScope

    private class ActionScopeImplementation : ActionScope
}

abstract class ParametrizedAction<T> : Action()
