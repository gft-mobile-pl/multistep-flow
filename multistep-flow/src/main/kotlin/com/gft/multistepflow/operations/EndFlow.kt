package com.gft.multistepflow.operations

import com.gft.coroutines.ErrorIgnoringScope
import com.gft.coroutines.firstOf
import com.gft.multistepflow.MultiStepFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

class EndFlow internal constructor(val flow: MultiStepFlow<*>) {
    suspend operator fun invoke() {
        if (coroutineContext.isPerformActionContext(flow)) {
            val onEnd = Channel<Unit>()
            ErrorIgnoringScope().launch {
                firstOf(
                    {
                        flow.session.data.first { data -> data == null }
                    },
                    {
                        flow.mutex.withLock {
                            if (flow.session.isStarted) flow.session.end()
                        }
                    }
                )
                onEnd.send(Unit)
            }
            flow.mutex.unlock()
            onEnd.receive()
        } else {


            flow.mutex.withLock {
                flow.session.end()
            }
        }
    }
}
