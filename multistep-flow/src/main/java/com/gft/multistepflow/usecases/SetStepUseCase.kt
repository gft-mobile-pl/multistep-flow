package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import kotlin.reflect.KClass

class SetStepUseCase<FlowStepType : StepType<*, *, *, *>>(
    private val flow: MultiStepFlow<FlowStepType>
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true
    ) {
        flow.session.update { flowState ->
            if (flowState.currentStep.type == step.type) {
                flowState.copy(
                    currentStep = if (reuseUserInput) {
                        (step as Step<*, *, Any?, *, *>).copy(userInput = flowState.currentStep.userInput)
                    } else {
                        step
                    },
                    previousSteps = if (flow.historyEnabled) flowState.previousSteps.replaceLast(step) else flowState.previousSteps
                )
            } else {
                flowState.copy(
                    currentStep = step,
                    previousSteps = if (flow.historyEnabled) flowState.previousSteps + step else flowState.previousSteps
                )
            }
        }
    }

    operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
        clearHistoryTo: FlowStepType,
        clearHistoryInclusive: Boolean
    ) {
        this(
            step = step,
            reuseUserInput = reuseUserInput,
            clearHistoryTo = flow.session.requireData()
                .previousSteps
                .lastOrNull { stepFromHistory -> stepFromHistory.type::class == clearHistoryTo::class }
                ?: throw IllegalArgumentException("There is no step of type $clearHistoryTo in the history."),
            clearHistoryInclusive
        )
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
        clearHistoryTo: Step<*, *, *, *, *>,
        clearHistoryInclusive: Boolean
    ) {
        flow.session.update { flowState ->
            if (!flowState.previousSteps.contains(clearHistoryTo)) throw IllegalArgumentException("Step $clearHistoryTo cannot be found in the history.")

            val newHistory = flowState.previousSteps.popTo(clearHistoryTo, clearHistoryInclusive)
            val currentStep = newHistory.lastOrNull()
            if (currentStep?.type == step.type) {
                flowState.copy(
                    currentStep = if (reuseUserInput) {
                        (step as Step<*, *, Any?, *, *>).copy(userInput = currentStep.userInput)
                    } else {
                        step
                    },
                    previousSteps = if (flow.historyEnabled) newHistory.replaceLast(step) else flowState.previousSteps
                )
            } else {
                flowState.copy(
                    currentStep = step,
                    previousSteps = if (flow.historyEnabled) newHistory + step else flowState.previousSteps
                )
            }
        }
    }
}

private fun <T> List<T>.popTo(item: T, inclusive: Boolean): List<T> {
    val itemIndex = lastIndexOf(item)
    return slice(0..(if (inclusive) itemIndex - 1 else itemIndex))
}

private fun <T> List<T>.replaceLast(item: T) = slice(0 until lastIndex) + item