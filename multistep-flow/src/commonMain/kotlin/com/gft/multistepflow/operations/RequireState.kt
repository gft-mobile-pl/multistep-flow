package com.gft.multistepflow.operations

import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.StepType

class RequireState<FlowStepType : StepType<*, *, *, *>> internal constructor(private val flow: MultiStepFlow<FlowStepType>) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke() = flow.session.requireData() as FlowState<FlowStepType, *, *, *>
}