package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.runBlocking
import org.junit.Test

class StartMultiStepFlowUseCaseTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlowUseCase<TestStepType<*, *, *, *>>

    @Test
    fun `when the flow is started and history is disabled then history is empty`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestStepType.TestFirstStepType))
            assert(testFlow.session.requireData().stepsHistory.isEmpty())
        }
    }

    @Test
    fun `when the flow is started and history is enabled then history is not empty and starting step is in history`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestStepType.TestFirstStepType))
            assert(testFlow.session.requireData().stepsHistory == listOf(Step(TestStepType.TestFirstStepType)))
        }
    }
}
