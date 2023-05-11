package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.GetCurrentStepUseCaseTest.TestStepType.*
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetCurrentStepUseCaseTest {
    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestThirdStepType : TestStepType<String, Int, Boolean, DefaultNoOpValidator>
        object TestFourthStepType : TestStepType<String, Double, Boolean, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var getCurrentStep: GetCurrentStepUseCase

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
        startMultiStepFlow = StartMultiStepFlow(testFlow)
        getCurrentStep = GetCurrentStepUseCase(testFlow)
        setStep = SetStepUseCase(testFlow)
    }

    @Test
    fun `when flow is started then current step is returned`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))

            //when
            val currentStep = getCurrentStep()

            //then
            assert(currentStep == Step(TestFirstStepType))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `when flow is not started then exception is thrown`() {
        runTest {
            val currentSte = getCurrentStep()
        }
    }
}