package com.gft.multistepflow.operations

import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import java.util.UUID
import kotlin.coroutines.coroutineContext

class RestartFlow<FlowStepType : StepType<*, *, *, *>> internal constructor(
    private val flow: MultiStepFlow<FlowStepType>,
) {
    suspend operator fun <StepType : FlowStepType> invoke(
        initialStep: Step<StepType, *, *, *, *>,
    ) {
        if (!coroutineContext.isPerformActionContext(flow)) {
            throw IllegalFlowException("MultiStepFlow<*>.restartFlow(Step) can only be called within an Action that was started in the context of that Flow.")
        }

        flow.session.update {
            initialStep.flow = flow
            FlowState(
                currentStep = initialStep as Step<*, *, *, *, *>,
                currentActionJob = null,
                currentActionType = null,
                stepsHistory = if (flow.historyEnabled) listOf(initialStep) else emptyList(),
                lifecycleState = Lifecycle.State.Started(UUID.randomUUID().toString()),
                error = null
            )
        }
    }
}