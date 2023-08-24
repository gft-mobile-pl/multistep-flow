package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.Action
import com.gft.multistepflow.model.ActionError
import com.gft.multistepflow.model.MultiStepFlow
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
    ): Unit = withContext(NonCancellable) {
        flow.mutex.withLock {
            flow.session.update { flowState ->
                flowState.copy(isAnyOperationInProgress = true)
            }
            try {
                action.internalPerform(transactionId)
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
                    throw error
                }
            }
        }
    }
}
