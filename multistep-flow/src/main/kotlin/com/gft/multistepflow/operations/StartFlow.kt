package com.gft.multistepflow.operations

import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import kotlinx.coroutines.flow.first
import java.util.UUID

class StartFlow<FlowStepType : StepType<*, *, *, *>> internal constructor(
    private val flow: MultiStepFlow<FlowStepType>,
) {
    suspend operator fun invoke(
        initialStep: Step<out FlowStepType, *, *, *, *>,
    ): Result<Unit> {
        flow.lifecycle.first { state -> state !is Lifecycle.State.Clearing }
        return try {
            initialStep.flow = flow
            flow.session.start(
                FlowState(
                    currentStep = initialStep as Step<*, *, *, *, *>,
                    currentActionJob = null,
                    stepsHistory = if (flow.historyEnabled) listOf(initialStep) else emptyList(),
                    lifecycleState = Lifecycle.State.Started(UUID.randomUUID().toString())
                )
            )
            Result.success(Unit)
        } catch (error: Throwable) {
            initialStep.flow = null
            Result.failure(IllegalFlowStateException("Flow $flow is already started!"))
        }
    }
}
