package com.gft.multistepflow.model

abstract class Action {
    protected abstract suspend fun perform(transactionId: String)

    internal suspend fun internalPerform(transactionId: String) = perform(transactionId)

    override fun toString(): String = this::class.simpleName ?: super.toString()
}

abstract class ParametrizedAction<T> : Action()
