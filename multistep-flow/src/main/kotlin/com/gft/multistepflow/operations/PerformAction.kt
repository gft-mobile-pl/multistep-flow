package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.ActionError
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.NotActionErrorException
import com.gft.multistepflow.StepType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

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
    ): Unit = withContext(NonCancellable) {
        if (flow == null) {
            throw IllegalStateException("You must add the step to the flow before performing any action.")
        }
        if (!flow.session.isStarted) throw IllegalStateException("Action $action cannot be performed - flow is not started.")

        if (coroutineContext.isPerformActionContext(flow)) {
            throw InvalidFlowException(
                "Calling Step.performAction(Action) within another Action currently being performed in the scope of a flow to which the Step belongs is not allowed. " +
                        "If you need to launch the action immediately, use Step.performChildAction(Action) instead. " +
                        "To enqueue the action, ensure Step.performAction(Action) is invoked in a parallel scope, such as GlobalScope."
            )
        } else withContext(PerformActionContext(flow)) actionContent@{
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
    ): Unit = withContext(NonCancellable) {
        if (flow == null) {
            throw IllegalStateException("You must add the step to the flow before performing any action.")
        }
        if (!flow.session.isStarted) throw IllegalStateException("Action $action cannot be performed - flow is not started.")

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