package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.ActionError
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.NotActionErrorException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private class PerformActionContext(
    val flow: MultiStepFlow<*>,
) : AbstractCoroutineContextElement(PerformActionContext) {
    companion object Key : CoroutineContext.Key<PerformActionContext>
}

internal fun CoroutineContext.isPerformActionContext(flow: MultiStepFlow<*>): Boolean = this[PerformActionContext]?.flow == flow

internal object PerformAction {
    suspend fun performAction(
        action: Action<*, *>,
        flow: MultiStepFlow<*>,
        dispatcher: CoroutineDispatcher?,
        transactionId: String,
    ): Unit = withContext(NonCancellable) {
        if (!flow.session.isStarted) throw IllegalStateException("Action $action cannot be performed - flow is not started.")

        if (coroutineContext.isPerformActionContext(flow)) {
            if (dispatcher != null) {
                withContext(dispatcher) {
                    action.internalPerform(flow, transactionId)
                }
            } else {
                action.internalPerform(flow, transactionId)
            }
        } else withContext(PerformActionContext(flow)) actionContent@ {
            flow.mutex.lock()

            flow.session.update { flowState ->
                flowState.copy(isAnyOperationInProgress = true)
            }
            try {
                if (dispatcher != null) {
                    withContext(dispatcher) {
                        action.internalPerform(flow, transactionId)
                    }
                } else {
                    action.internalPerform(flow, transactionId)
                }

                if (!flow.session.isStarted) return@actionContent
                flow.session.update { flowState ->
                    flowState.copy(isAnyOperationInProgress = false)
                }
            } catch (error: Throwable) {
                if (!flow.session.isStarted) {
                    throw IllegalStateException("Cannot handle action error, as flow has already ended.", error)
                }

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
            } finally {
                try {
                    flow.mutex.unlock()
                } catch (error: Throwable) {
                    // nothing - mutex was unlocked by some other action internally
                }
            }
        }
    }
}
