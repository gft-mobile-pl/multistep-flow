package com.gft.multistepflow.operations

import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.requireState
import com.gft.multistepflow.start
import kotlinx.coroutines.flow.first
import java.util.UUID

class StartFlow<FlowStepType : StepType<*, *, *, *>> internal constructor(
    private val flow: MultiStepFlow<FlowStepType>,
) {
    suspend operator fun invoke(
        initialStep: Step<out FlowStepType, *, *, *, *>,
        validateUserInput: Boolean = false
    ): Result<Unit> {
        if (initialStep.flow != null && initialStep.flow != flow) {
            throw IllegalArgumentException("Step can be added to one flow only. Copy the step if you need to add the same step to more than one flow.")
        }

        flow.lifecycle.first { state -> state !is Lifecycle.State.Clearing }
        return try {
            // validate initial step (if required) on a side flow to start with an already validated step
            val updatedInitialStep = if (validateUserInput && initialStep.userInputValidator != null) {
                val validationFlow = MultiStepFlow<FlowStepType>(historyEnabled = flow.historyEnabled)
                validationFlow.start(
                    initialStep = initialStep,
                    validateUserInput = false
                )
                validationFlow.requireState().currentStep.validate()
            } else {
                initialStep
            }

            updatedInitialStep.flow = flow
            flow.session.start(
                FlowState(
                    currentStep = updatedInitialStep as Step<*, *, *, *, *>,
                    currentActionJob = null,
                    currentActionType = null,
                    stepsHistory = if (flow.historyEnabled) listOf(updatedInitialStep) else emptyList(),
                    lifecycleState = Lifecycle.State.Started(UUID.randomUUID().toString()),
                    error = null
                )
            )
            Result.success(Unit)
        } catch (error: Throwable) {
            initialStep.flow = null
            Result.failure(IllegalFlowStateException("Flow $flow is already started!"))
        }
    }

    suspend operator fun invoke(
        stepsHistory: List<Step<out FlowStepType, *, *, *, *>>,
        validateUserInput: Boolean = false
    ): Result<Unit> {
        if (!flow.historyEnabled)
            throw IllegalFlowException("Cannot start the flow using provided steps history as steps history feature is disabled for the current flow.")
        if (stepsHistory.isEmpty())
            throw IllegalArgumentException("Cannot start the flow as the provided steps history is empty. Initial step is required  to start a flow.")
        stepsHistory.forEach { step ->
            if (step.flow != null && step.flow != flow) {
                throw IllegalArgumentException("Step can be added to one flow only. Copy the step if you need to add the same step to more than one flow.")
            }
        }

        flow.lifecycle.first { state -> state !is Lifecycle.State.Clearing }

        return try {
            // validate each step (if required) on a side flow to start with an already validated steps
            val updatedInitialSteps = if (validateUserInput) {
                val validationFlow = MultiStepFlow<FlowStepType>(historyEnabled = true)
                validationFlow.start(
                    stepsHistory = stepsHistory,
                    validateUserInput = false
                )
                validationFlow.requireState().stepsHistory.map { step ->
                    if (step.userInputValidator != null) step.validate()
                    else step
                }
            } else {
                stepsHistory
            }

            @Suppress("UNCHECKED_CAST")
            flow.session.start(
                FlowState(
                    currentStep = updatedInitialSteps.last() as Step<*, *, *, *, *>,
                    currentActionJob = null,
                    currentActionType = null,
                    stepsHistory = updatedInitialSteps.onEach { step -> (step as Step<FlowStepType, *, *, *, *>).flow = flow },
                    lifecycleState = Lifecycle.State.Started(UUID.randomUUID().toString()),
                    error = null
                )
            )
            Result.success(Unit)
        } catch (error: Throwable) {
            stepsHistory.onEach { step -> step.flow = null }
            Result.failure(IllegalFlowStateException("Flow $flow is already started!"))
        }
    }
}
