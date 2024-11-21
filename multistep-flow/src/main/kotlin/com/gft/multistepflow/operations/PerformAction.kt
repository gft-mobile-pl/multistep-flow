package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.ActionError
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.NotActionErrorException
import com.gft.multistepflow.StepType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private class PerformActionContext(
    val flow: MultiStepFlow<*>,
) : AbstractCoroutineContextElement(PerformActionContext) {
    companion object Key : CoroutineContext.Key<PerformActionContext>
}

internal fun CoroutineContext.isPerformActionContext(flow: MultiStepFlow<*>): Boolean = this[PerformActionContext]?.flow == flow

class PerformAction<Type : StepType<*, *, *, *>> internal constructor(
    internal val flow: MultiStepFlow<in Type>?,
) {
    suspend operator fun invoke(
        action: Action<in Type, in Type>,
        transactionId: String = UUID.randomUUID().toString(),
    ) = performActionImplementation(action = action, null, transactionId = transactionId)

    suspend operator fun <Type : StepType<*, *, *, *>> invoke(
        action: Action<in Type, in Type>,
        dispatcher: CoroutineDispatcher,
        transactionId: String = UUID.randomUUID().toString(),
    ) = performActionImplementation(action = action, dispatcher = dispatcher, transactionId = transactionId)

    private suspend fun performActionImplementation(
        action: Action<*, *>,
        dispatcher: CoroutineDispatcher?,
        transactionId: String,
    ) {
        if (flow == null) {
            throw IllegalStateException("You must add the step to the flow before performing any action.")
        }

        val sessionId = (flow.session.data.value?.lifecycleState as? Lifecycle.State.Started)?.sessionId
            ?: throw IllegalStateException("Action $action cannot be performed - flow is not started.")

        if (coroutineContext.isPerformActionContext(flow)) {
            throw InvalidFlowException(
                "Calling Step.performAction(Action) within another Action currently being performed in the scope of a flow to which the Step belongs is not allowed. " +
                        "If you need to launch the action immediately, use Step.performChildAction(Action) instead. " +
                        "To enqueue the action, ensure Step.performAction(Action) is invoked in a parallel scope, such as GlobalScope."
            )
        } else withContext(PerformActionContext(flow)) actionContent@{
            supervisorScope {
                flow.mutex.lock()

                var unhandledError: NotActionErrorException? = null

                val actionJob = async(start = CoroutineStart.LAZY) {
                    if (dispatcher != null) {
                        withContext(dispatcher) {
                            action.internalPerform(flow, transactionId)
                        }
                    } else {
                        action.internalPerform(flow, transactionId)
                    }
                }

                actionJob.invokeOnCompletion { error ->
                    when (error) {
                        is ActionError -> flow.session.update { flowState ->
                            flowState.copy(
                                currentActionJob = null,
                                currentStep = flowState.currentStep.copy(
                                    error = error
                                )
                            )
                        }

                        is ClearFlowException -> {
                            // flow is about to end or has ended
                        }

                        null, is CancellationException -> {
                            flow.session.update { flowState ->
                                flowState.copy(currentActionJob = null)
                            }
                        }

                        else -> unhandledError = NotActionErrorException(error, action)
                    }

                    flow.mutex.unlock()
                }

                var skipAction = false
                try {
                    flow.session.update { flowState ->
                        if (sessionId != flowState.lifecycleState.sessionId) {
                            // flow is clearing or was restarted
                            skipAction = true
                            flowState
                        } else {
                            skipAction = false
                            flowState.copy(currentActionJob = actionJob)
                        }
                    }
                } catch (error: Throwable) {
                    // flow has ended in the meantime
                    skipAction = true
                }

                if (skipAction) {
                    actionJob.cancel(ClearFlowException())
                    return@supervisorScope
                }

                actionJob.join()
                unhandledError?.apply { throw this }
            }
        }
    }
}

class PerformChildAction<Type : StepType<*, *, *, *>> internal constructor(
    internal val flow: MultiStepFlow<in Type>?,
) {
    suspend operator fun invoke(
        action: Action<in Type, in Type>,
        transactionId: String = UUID.randomUUID().toString(),
    ) = performActionImplementation(action = action, null, transactionId = transactionId)

    suspend operator fun <Type : StepType<*, *, *, *>> invoke(
        action: Action<in Type, in Type>,
        dispatcher: CoroutineDispatcher,
        transactionId: String = UUID.randomUUID().toString(),
    ) = performActionImplementation(action = action, dispatcher = dispatcher, transactionId = transactionId)

    private suspend fun performActionImplementation(
        action: Action<*, *>,
        dispatcher: CoroutineDispatcher?,
        transactionId: String,
    ) {
        if (flow == null) {
            throw IllegalStateException("You must add the step to the flow before performing any action.")
        }

        if (coroutineContext.isPerformActionContext(flow)) {
            if (dispatcher != null) {
                withContext(dispatcher) {
                    action.internalPerform(flow, transactionId)
                }
            } else {
                action.internalPerform(flow, transactionId)
            }
        } else {
            throw InvalidFlowException(
                "Step.performChildAction(Action) may only be called within an Action currently being performed in the scope of a flow to which the Step belongs. " +
                        "If you intend to perform an Action on a different flow, opt-in to Step.performAction(Action)."
            )
        }
    }
}