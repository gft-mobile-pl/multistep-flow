package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import kotlinx.coroutines.flow.map

open class IsFlowStartedUseCase(
    private val flow: MultiStepFlow<*>
) {
    operator fun invoke() = flow.session.data.map { state -> state != null }
}
