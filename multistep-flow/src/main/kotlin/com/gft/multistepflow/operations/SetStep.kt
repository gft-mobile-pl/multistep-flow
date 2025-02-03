package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.utils.replaceLast
import kotlin.coroutines.coroutineContext

class SetStep<FlowStepType : StepType<*, *, *, *>> internal constructor(
    private val flow: MultiStepFlow<FlowStepType>,
) {
    @Suppress("UNCHECKED_CAST")
    suspend operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
    ) {
        if (!coroutineContext.isPerformActionContext(flow)) {
            throw IllegalFlowException("MultiStepFlow<*>.setStep(Step, Boolean) can only be called within an Action that was started in the context of that Flow.")
        }

        step.flow = flow

        flow.session.update { flowState ->
            if (flowState.currentStep.type == step.type) {
                val stepToSet = if (reuseUserInput) {
                    (step as Step<*, *, Any?, *, *>).copyWithFlow(userInput = flowState.currentStep.userInput)
                } else {
                    step
                }
                flowState.copy(
                    currentStep = stepToSet,
                    stepsHistory = if (flow.historyEnabled) flowState.stepsHistory.replaceLast(stepToSet) else flowState.stepsHistory
                )
            } else {
                flowState.copy(
                    currentStep = step as Step<*, *, *, *, *>,
                    stepsHistory = if (flow.historyEnabled) flowState.stepsHistory + step else flowState.stepsHistory
                )
            }
        }
    }

    suspend operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
        clearHistoryTo: FlowStepType,
        clearHistoryInclusive: Boolean,
    ) {
        this(
            step = step,
            reuseUserInput = reuseUserInput,
            clearHistoryTo = flow.session.requireData()
                .stepsHistory
                .lastOrNull { stepFromHistory -> stepFromHistory.type::class == clearHistoryTo::class }
                ?: throw IllegalArgumentException("There is no step of type $clearHistoryTo in the history."),
            clearHistoryInclusive
        )
    }

    @Suppress("UNCHECKED_CAST")
    suspend operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
        clearHistoryTo: Step<*, *, *, *, *>,
        clearHistoryInclusive: Boolean,
    ) {
        if (!coroutineContext.isPerformActionContext(flow)) {
            throw IllegalFlowException("MultiStepFlow<*>.setStep(Step, Boolean) can only be called within an Action that was started in the context of that Flow.")
        }

        flow.session.update { flowState ->
            if (!flowState.stepsHistory.contains(clearHistoryTo)) throw IllegalArgumentException("Step $clearHistoryTo cannot be found in the history.")

            step.flow = flow

            val newHistory = flowState.stepsHistory.popTo(clearHistoryTo, clearHistoryInclusive)
            val currentStep = newHistory.lastOrNull()
            if (currentStep?.type == step.type) {
                val stepToSet = if (reuseUserInput) {
                    (step as Step<*, *, Any?, *, *>).copyWithFlow(userInput = currentStep.userInput)
                } else {
                    step
                }
                flowState.copy(
                    currentStep = stepToSet,
                    stepsHistory = if (flow.historyEnabled) newHistory.replaceLast(stepToSet) else flowState.stepsHistory
                )
            } else {
                flowState.copy(
                    currentStep = step as Step<*, *, *, *, *>,
                    stepsHistory = if (flow.historyEnabled) newHistory + step else flowState.stepsHistory
                )
            }
        }
    }
}

private fun <T> List<T>.popTo(item: T, inclusive: Boolean): List<T> {
    val itemIndex = lastIndexOf(item)
    return slice(0..(if (inclusive) itemIndex - 1 else itemIndex))
}
