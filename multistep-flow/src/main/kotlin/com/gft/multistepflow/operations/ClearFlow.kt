package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class ClearFlow internal constructor(val flow: MultiStepFlow<*>) {
    suspend operator fun invoke() = withContext(NonCancellable) {
        var sessionId = ""
        var isClearingAlready = false

        try {
            flow.session.update { flowState ->
                isClearingAlready = flowState.lifecycleState is Lifecycle.State.Clearing
                if (isClearingAlready) {
                    sessionId = flowState.lifecycleState.sessionId
                    flowState
                } else {
                    sessionId = UUID.randomUUID().toString()
                    flowState.currentActionJob?.cancel(ClearFlowException())
                    flowState.copy(lifecycleState = Lifecycle.State.Clearing(sessionId))
                }
            }
        } catch (error: Throwable) {
            // flow is stopped already
            return@withContext
        }

        if (coroutineContext.isPerformActionContext(flow)) {
            if (!isClearingAlready) {
                flow.session.end()
            }

            // throwing ClearFlowException will end the current action immediately even if it is not cancellable
            throw ClearFlowException()
        } else {
            if (isClearingAlready) {
                flow.lifecycle.first { state ->
                    sessionId != state.sessionId
                }
            } else {
                flow.session.data.value?.currentActionJob?.join()

                // we need to check the sessionId as flow could be restarted (with MultiStepFlow.Restart()) in the meantime (e.g. by a non cancellable action)
                if (sessionId == flow.lifecycle.value.sessionId) {
                    flow.session.end()
                }
            }
        }
    }
}

internal class ClearFlowException : CancellationException()