package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow

class ClearError internal constructor(
    private val flow: MultiStepFlow<*>
) {
    operator fun invoke() {
        flow.session.update { flowState ->
            flowState.copy(
                error = null
            )
        }
    }
}