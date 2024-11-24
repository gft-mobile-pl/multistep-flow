package com.gft.multistepflow

import com.gft.multistepflow.annotations.ClearFlowInActionScope
import com.gft.multistepflow.annotations.FlowStartInActionScope
import com.gft.multistepflow.annotations.PerformActionInActionScope
import com.gft.multistepflow.operations.ClearFlow
import com.gft.multistepflow.operations.PerformAction
import com.gft.multistepflow.operations.PerformChildAction
import com.gft.multistepflow.operations.RestartFlow
import com.gft.multistepflow.operations.SetStep
import com.gft.multistepflow.operations.StartFlow

abstract class Action<SupportedStep, FlowStepType : StepType<*, *, *, *>> {

    protected abstract suspend fun perform(
        flow: MultiStepFlow<FlowStepType>,
        transactionId: String,
    )

    internal open suspend fun internalPerform(
        flow: MultiStepFlow<*>,
        transactionId: String,
    ) {
        // We rely on Step.performAction API to do the type check
        @Suppress("UNCHECKED_CAST")
        perform(
            flow = flow as MultiStepFlow<FlowStepType>,
            transactionId = transactionId
        )
    }

    override fun toString(): String = this::class.simpleName ?: super.toString()

    @FlowStartInActionScope
    val MultiStepFlow<FlowStepType>.start
        get() = StartFlow(this)

    @PerformActionInActionScope
    val <Type : FlowStepType> Step<Type, *, *, *, *>.performAction: PerformAction<Type>
        get() = PerformAction(flow)

    @ClearFlowInActionScope
    val <Type: FlowStepType> MultiStepFlow<Type>.clear: ClearFlow
        get() = ClearFlow(this)

    val <Type: FlowStepType> Step<Type, *, *, *, *>.performChildAction: PerformChildAction<Type>
        get() = PerformChildAction(flow)

    val MultiStepFlow<*>.end
        get() = ClearFlow(this)

    val <T : FlowStepType> MultiStepFlow<T>.setStep
        get() = SetStep(this)

    val <T : FlowStepType> MultiStepFlow<T>.restartFlow
        get() = RestartFlow(this)
}

abstract class MultiFlowAction<SupportedStep, FlowStepType : StepType<*, *, *, *>> : Action<SupportedStep, FlowStepType>() {
    override suspend fun perform(flow: MultiStepFlow<FlowStepType>, transactionId: String) {
        perform(flow, transactionId)
    }

    protected abstract suspend fun performAction(flow: MultiStepFlow<out FlowStepType>, transactionId: String)
}
