package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.sync.withLock

class EndMultiStepFlow<FlowStepType : StepType<*, *, *, *>>(
    private val flow: MultiStepFlow<FlowStepType>
) {
    suspend operator fun <StepType : FlowStepType> invoke() = flow.mutex.withLock {
        flow.session.end()
    }
}