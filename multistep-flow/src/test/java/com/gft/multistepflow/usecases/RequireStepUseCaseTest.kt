package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RequireStepUseCaseTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestThirdStepType : TestStepType<String, Int, Boolean, DefaultNoOpValidator>
        object TestFourthStepType : TestStepType<String, Double, Boolean, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var requireStep: RequireStepUseCase
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
        startMultiStepFlow = StartMultiStepFlow(testFlow)
        requireStep = RequireStepUseCase(testFlow)
        setStep = SetStepUseCase(testFlow)
    }

    @Test
    fun `when specific current step type is requested and it is current step then it is returned`() {
        runTest {

            //given
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))

            //when
            val currentStep = requireStep(TestStepType.TestSecondStepType)

            //then
            assert(currentStep == Step(TestStepType.TestSecondStepType))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when specific current step type is requested and it is not current step then exception is thrown`() {
        runTest {

            //given
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))

            //when
            val currentStep = requireStep(TestStepType.TestFirstStepType)

            //then exception is thrown
        }
    }

    @Test
    fun `when multiple step types are requested and one of them is current step then it is returned`() {
        runTest {

            //given
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(Step(TestStepType.TestThirdStepType, "payload", 5, true))

            //when
            val currentStep = requireStep(TestStepType.TestThirdStepType, TestStepType.TestFourthStepType)

            //then
            assert(currentStep == Step(TestStepType.TestThirdStepType, "payload", 5, true))
        }
    }
}