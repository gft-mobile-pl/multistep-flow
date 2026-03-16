package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.ActionError
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.NotActionErrorException
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.utils.randomUUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
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
        transactionId: String = randomUUID(),
    ) = performActionImplementation(action = action, null, transactionId = transactionId)

    suspend operator fun <Type : StepType<*, *, *, *>> invoke(
        action: Action<in Type, in Type>,
        dispatcher: CoroutineDispatcher,
        transactionId: String = randomUUID(),
    ) = performActionImplementation(action = action, dispatcher = dispatcher, transactionId = transactionId)

    private suspend fun performActionImplementation(
        action: Action<*, *>,
        dispatcher: CoroutineDispatcher?,
        transactionId: String,
    ): Result<Step<*, *, *, *, *>?> {
        if (flow == null) {
            throw IllegalStateException("You must add the step to the flow before performing any action.")
        }

        if (coroutineContext.isPerformActionContext(flow)) {
            throw IllegalFlowException(
                "Calling Step.performAction(Action) within an Action being performed in the scope of a flow to which the Step belongs is not allowed. " +
                        "If you need to launch the action immediately, use Step.performChildAction(Action) instead. "
            )
        }

        return try {
            withContext(PerformActionContext(flow)) {
                val actionJob = async(start = CoroutineStart.LAZY) {
                    if (dispatcher != null) {
                        withContext(dispatcher) {
                            action.internalPerform(flow, transactionId)
                        }
                    } else {
                        action.internalPerform(flow, transactionId)
                    }
                    flow.onActionFinished()
                    flow.session.data.value!!.currentStep
                }

                flow.session.update { flowState ->
                    if (flowState.lifecycleState is Lifecycle.State.Started) {
                        if (flowState.currentActionJob != null) {
                            throw AnotherActionInProgressException()
                        } else {
                            flowState.copy(
                                currentActionJob = actionJob,
                                currentActionType = action::class
                            )
                        }
                    } else {
                        throw IllegalFlowStateException("Action $action cannot be performed - flow is not started.")
                    }
                }

                Result.success(actionJob.await())
            }
        } catch (error: Throwable) {
            if (flow.lifecycle.value is Lifecycle.State.NotInitialized) {
                // flow is already cleared
                // this scenario happens when `MultiStepFlow.clear` is called inside an Action
                // we consider this as success
                Result.success(null)
            } else {
                when (error) {
                    // action cancelled externally
                    is CancellationException -> {
                        flow.onActionFinished()
                        Result.failure(error)
                    }

                    // action failed in a controlled way
                    is ActionError -> {
                        flow.onActionFailed(error)
                        Result.failure(error)
                    }

                    // action tried to launch child action which belongs to another flow
                    is IllegalFlowException -> {
                        throw error
                    }

                    is IllegalFlowStateException -> {
                        Result.failure(error)
                    }

                    // action cannot be started until previous action is complete
                    is AnotherActionInProgressException -> {
                        Result.failure(error)
                    }

                    // any other error thrown in uncontrolled way
                    else -> throw NotActionErrorException(error, action)
                }
            }
        }
    }

    private fun MultiStepFlow<*>.onActionFinished(): MultiStepFlow<*> {
        runCatching {
            session.update { flowState ->
                flowState.copy(
                    currentActionJob = null,
                    currentActionType = null
                )
            }
        }
        return this
    }

    private fun MultiStepFlow<*>.onActionFailed(error: ActionError) = session.update { flowState ->
        flowState.copy(
            currentActionJob = null,
            currentActionType = null,
            error = error
        )
    }
}

class PerformChildAction<Type : StepType<*, *, *, *>> internal constructor(
    internal val flow: MultiStepFlow<in Type>?,
) {
    suspend operator fun invoke(
        action: Action<in Type, in Type>,
        transactionId: String = randomUUID(),
    ) = performActionImplementation(action = action, null, transactionId = transactionId)

    suspend operator fun <Type : StepType<*, *, *, *>> invoke(
        action: Action<in Type, in Type>,
        dispatcher: CoroutineDispatcher,
        transactionId: String = randomUUID(),
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
            throw IllegalFlowException(
                "Step.performChildAction(Action) may only be called within an Action currently being performed in the scope of a flow to which the Step belongs. " +
                        "If you intend to perform an Action on a different flow, opt-in to Step.performAction(Action)."
            )
        }
    }
}
