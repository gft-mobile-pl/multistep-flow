package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.StepType

class RequireState<FlowStepType : StepType<*, *, *, *>> internal constructor(private val flow: MultiStepFlow<FlowStepType>) {
    operator fun invoke() = flow.session.requireData()
}