package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.ActionError
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.NotActionErrorException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private class PerformActionContext : AbstractCoroutineContextElement(PerformActionContext) {
    companion object Key : CoroutineContext.Key<PerformActionContext>
}

internal val CoroutineContext.isPerformActionContext: Boolean
    get() = this[PerformActionContext] != null

internal class PerformAction(
    private val flow: MultiStepFlow<*>,
) {
    suspend fun performAction(
        action: Action<*, *>,
        dispatcher: CoroutineDispatcher?,
        transactionId: String,
    ): Unit = withContext(NonCancellable) {
        if (coroutineContext.isPerformActionContext) {
            if (dispatcher != null) {
                withContext(dispatcher) {
                    action.internalPerform(transactionId)
                }
            } else {
                action.internalPerform(transactionId)
            }
        } else withContext(PerformActionContext()) {
            flow.mutex.withLock {
                flow.session.update { flowState ->
                    flowState.copy(isAnyOperationInProgress = true)
                }
                try {
                    if (dispatcher != null) {
                        withContext(dispatcher) {
                            action.internalPerform(transactionId)
                        }
                    } else {
                        action.internalPerform(transactionId)
                    }
                    flow.session.update { flowState ->
                        flowState.copy(isAnyOperationInProgress = false)
                    }
                } catch (error: Throwable) {
                    if (error is ActionError) {
                        flow.session.update { flowState ->
                            flowState.copy(
                                isAnyOperationInProgress = false,
                                currentStep = flowState.currentStep.copy(
                                    error = error
                                )
                            )
                        }
                    } else {
                        throw NotActionErrorException(error, action)
                    }
                }
            }
        }
    }
}
