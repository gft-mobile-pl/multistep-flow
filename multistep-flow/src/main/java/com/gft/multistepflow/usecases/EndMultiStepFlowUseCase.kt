package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import kotlinx.coroutines.sync.withLock

open class EndMultiStepFlowUseCase(
    private val flow: MultiStepFlow<*>
) {
    suspend operator fun invoke() = flow.mutex.withLock {
        flow.session.end()
    }
}