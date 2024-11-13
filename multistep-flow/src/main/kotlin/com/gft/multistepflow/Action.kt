package com.gft.multistepflow

import com.gft.multistepflow.annotations.FlowStartInActionScope
import com.gft.multistepflow.operations.StartFlow

abstract class Action<SupportedStep, FlowType : MultiStepFlow<*>> {

    protected abstract suspend fun perform(
        flow: FlowType,
        transactionId: String,
    )

    internal open suspend fun internalPerform(
        flow: MultiStepFlow<*>,
        transactionId: String,
    ) {
        // We rely on Step.performAction API to do the type check
        @Suppress("UNCHECKED_CAST")
        perform(
            flow = flow as FlowType,
            transactionId = transactionId
        )
    }

    override fun toString(): String = this::class.simpleName ?: super.toString()

    @FlowStartInActionScope
    val <FlowStepType : StepType<*, *, *, *>> MultiStepFlow<FlowStepType>.start
        get() = StartFlow(this)
}

abstract class MultiFlowAction<SupportedStep, FlowStepType : StepType<*, *, *, *>> :
    Action<SupportedStep, MultiStepFlow<out FlowStepType>>()
