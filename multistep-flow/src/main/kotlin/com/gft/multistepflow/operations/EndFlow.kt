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
                "MultiStepFlow<*>.endImmediately() can only be called within an Action that was started in the context of that Flow. " +
                        "If you intend to end a different flow, opt in to MultiStepFlow<*>.end()."
            )
        }
        flow.session.end()
    }
}
