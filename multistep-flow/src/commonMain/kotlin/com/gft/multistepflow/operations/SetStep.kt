package com.gft.multistepflow.operations

import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.requireState
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.replaceLast
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

class SetStep<FlowStepType : StepType<*, *, *, *>> internal constructor(
    private val flow: MultiStepFlow<FlowStepType>,
) {
    suspend operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
        validateUserInput: Boolean = false
    ) {
        if (!coroutineContext.isPerformActionContext(flow)) {
            throw IllegalFlowException("MultiStepFlow<*>.setStep(Step, Boolean) can only be called within an Action that was started in the context of that Flow.")
        }
        if (step.flow != null && step.flow != flow) {
            throw IllegalArgumentException("Step can be added to one flow only. Copy the step if you need to add the same step to more than one flow.")
        }

        flow.session.update { flowState ->
            val validatedStep = if (validateUserInput && !reuseUserInput && step.userInputValidator != null) {
                validateNewStep(
                    newStep = step,
                    clearHistoryTo = null,
                    clearHistoryInclusive = false
                )
            } else {
                step
            }

            validatedStep.flow = flow

            flowState.setStep(
                step = validatedStep,
                reuseUserInput = reuseUserInput
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun FlowState<*, *, *, *>.setStep(
        step: Step<*, *, *, *, *>,
        reuseUserInput: Boolean
    ): FlowState<*, *, *, *> = if (currentStep.type == step.type) {
        val stepToSet = if (reuseUserInput) {
            (step as Step<*, *, Any?, Any?, *>).copyWithFlow(
                userInput = currentStep.userInput,
                validationResult = currentStep.validationResult
            )
        } else {
            step
        }
        copy(
            currentStep = stepToSet,
            stepsHistory = if (flow.historyEnabled) stepsHistory.replaceLast(stepToSet) else stepsHistory
        )
    } else {
        copy(
            currentStep = step,
            stepsHistory = if (flow.historyEnabled) stepsHistory + step else stepsHistory
        )
    }

    suspend operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
        clearHistoryTo: FlowStepType,
        clearHistoryInclusive: Boolean,
        validateUserInput: Boolean = false
    ) {
        this(
            step = step,
            reuseUserInput = reuseUserInput,
            clearHistoryTo = flow.session.requireData()
                .stepsHistory
                .lastOrNull { stepFromHistory -> stepFromHistory.type::class == clearHistoryTo::class }
                ?: throw IllegalArgumentException("There is no step of type $clearHistoryTo in the history."),
            clearHistoryInclusive = clearHistoryInclusive,
            validateUserInput = validateUserInput
        )
    }

    suspend operator fun <StepType : FlowStepType> invoke(
        step: Step<StepType, *, *, *, *>,
        reuseUserInput: Boolean = true,
        clearHistoryTo: Step<*, *, *, *, *>,
        clearHistoryInclusive: Boolean,
        validateUserInput: Boolean = false
    ) {
        if (!coroutineContext.isPerformActionContext(flow)) {
            throw IllegalFlowException("MultiStepFlow<*>.setStep(Step, Boolean) can only be called within an Action that was started in the context of that Flow.")
        }
        if (step.flow != null && step.flow != flow) {
            throw IllegalArgumentException("Step can be added to one flow only. Copy the step if you need to add the same step to more than one flow.")
        }

        flow.session.update { flowState ->
            if (!flowState.stepsHistory.contains(clearHistoryTo)) throw IllegalArgumentException("Step $clearHistoryTo cannot be found in the history.")

            val validatedStep = if (validateUserInput && !reuseUserInput && step.userInputValidator != null) {
                validateNewStep(
                    newStep = step,
                    clearHistoryTo = clearHistoryTo,
                    clearHistoryInclusive = clearHistoryInclusive
                )
            } else {
                step
            }

            validatedStep.flow = flow

            flowState.setStep(
                step = validatedStep,
                reuseUserInput = reuseUserInput,
                clearHistoryTo = clearHistoryTo,
                clearHistoryInclusive = clearHistoryInclusive
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun FlowState<*, *, *, *>.setStep(
        step: Step<*, *, *, *, *>,
        reuseUserInput: Boolean,
        clearHistoryTo: Step<*, *, *, *, *>,
        clearHistoryInclusive: Boolean,
    ): FlowState<*, *, *, *> {
        val newHistory = stepsHistory.popTo(clearHistoryTo, clearHistoryInclusive)
        val currentStep = newHistory.lastOrNull()
        return if (currentStep?.type == step.type) {
            val stepToSet = if (reuseUserInput) {
                (step as Step<*, *, Any?, Any?, *>).copyWithFlow(
                    userInput = currentStep.userInput,
                    validationResult = currentStep.validationResult
                )
            } else {
                step
            }
            copy(
                currentStep = stepToSet,
                stepsHistory = if (flow.historyEnabled) newHistory.replaceLast(stepToSet) else stepsHistory
            )
        } else {
            copy(
                currentStep = step,
                stepsHistory = if (flow.historyEnabled) newHistory + step else stepsHistory
            )
        }
    }

    private fun validateNewStep(
        newStep: Step<*, *, *, *, *>,
        clearHistoryTo: Step<*, *, *, *, *>?,
        clearHistoryInclusive: Boolean,
    ): Step<FlowStepType, *, *, *, *> = try {
        val validationFlow = MultiStepFlow<StepType<*, *, *, *>>(historyEnabled = flow.historyEnabled)
        runBlocking {
            if (flow.historyEnabled) {
                validationFlow.start(
                    stepsHistory = flow.requireState().stepsHistory.onEach { step -> step.flow = null },
                    validateUserInput = false
                )
            } else {
                validationFlow.start(
                    initialStep = flow.requireState().currentStep.apply { flow = null },
                    validateUserInput = false
                )
            }
            newStep.flow = validationFlow
            validationFlow.session.update { flowState ->
                if (clearHistoryTo != null) flowState.setStep(newStep, false, clearHistoryTo, clearHistoryInclusive)
                else flowState.setStep(newStep, false)
            }
        }
        @Suppress("UNCHECKED_CAST")
        validationFlow.requireState().currentStep.validate() as Step<FlowStepType, *, *, *, *>
    } finally {
        flow.requireState().apply {
            @Suppress("UNCHECKED_CAST")
            currentStep.flow = flow as MultiStepFlow<in StepType<*, *, *, *>>
            stepsHistory.forEach { step ->
                step.flow = flow
            }
        }
    }
}

private fun <T> List<T>.popTo(item: T, inclusive: Boolean): List<T> {
    val itemIndex = lastIndexOf(item)
    return slice(0..(if (inclusive) itemIndex - 1 else itemIndex))
}
