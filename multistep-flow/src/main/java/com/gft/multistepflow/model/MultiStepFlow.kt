package com.gft.multistepflow.model

import com.gft.observablesession.Session
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

abstract class MultiStepFlow {
    internal val mutex: Mutex = Mutex()
    internal val session: Session<AltFlowPayload> = Session()

    init {
        //TODO("add isStarted")
    }

    internal data class AltFlowPayload(
        val currentStep: Step<*, *, *, *, *>,
        val isAnyOperationInProgress: Boolean
    )

    suspend fun start(
        currentStep: Step<*, *, *, *, *>
    ) = mutex.withLock {
        session.start(
            AltFlowPayload(
                currentStep = currentStep,
                isAnyOperationInProgress = false
            )
        )
    }

    suspend fun end() = withContext(NonCancellable) {
        mutex.withLock {
            session.end()
        }
    }

    override fun toString(): String {
        return "AltMultiStepFlow(mutex=$mutex, session=${session.data.value})"
    }
}