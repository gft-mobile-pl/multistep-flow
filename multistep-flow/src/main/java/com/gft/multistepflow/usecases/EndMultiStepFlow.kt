package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.sync.withLock

class EndMultiStepFlow(
    private val flow: MultiStepFlow<*>
) {
    suspend operator fun invoke() = flow.mutex.withLock {
        flow.session.end()
    }
}