package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.Action
import com.gft.multistepflow.model.ActionError
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.NotActionErrorException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

open class PerformActionUseCase(
    private val flow: MultiStepFlow<*>
) {
    suspend operator fun invoke(
        action: Action,
        transactionId: String = UUID.randomUUID().toString(),
    ): Unit = perform(action, null, transactionId)

    suspend operator fun invoke(
        action: Action,
        dispatcher: CoroutineDispatcher,
        transactionId: String = UUID.randomUUID().toString(),
    ): Unit = perform(action, dispatcher, transactionId)

    private suspend fun perform(
        action: Action,
        dispatcher: CoroutineDispatcher? = null,
        transactionId: String = UUID.randomUUID().toString(),
    ): Unit = withContext(NonCancellable) {
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
