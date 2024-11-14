package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

sealed interface EndFlow : suspend () -> Unit

internal class AwaitAllActionsAndEndFlow(val flow: MultiStepFlow<*>) : EndFlow {
    override suspend operator fun invoke() = flow.mutex.withLock {
        flow.session.end()
    }
}

internal class EndFlowImmediately(val flow: MultiStepFlow<*>) : EndFlow {
    override suspend operator fun invoke() {
        if (!coroutineContext.isPerformActionContext(flow)) {
            throw InvalidFlowException(
                "MultiStepFlow<*>.endImmediately() cannot be called within an Action that is not performed in the context of this Flow. " +
                        "If you intend to end a different flow, opt in to MultiStepFlow<*>.end()."
            )
        }
        flow.session.end()
    }
}
