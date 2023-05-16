package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.GetStepFromHistoryUseCaseTest.TestStepType.*
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetStepFromHistoryUseCaseTest {
    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlowUseCase<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var getStepFromHistory: GetStepFromHistoryUseCase

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
        startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
        getStepFromHistory = GetStepFromHistoryUseCase(testFlow)
        setStep = SetStepUseCase(testFlow)
    }

    @Test
    fun `when requested step is in history then it is returned`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestThirdStepType, "Payload", 5))
            setStep(Step(TestSecondStepType))

            //when
            val stepFromHistory = getStepFromHistory(TestThirdStepType)

            //then
            assert(stepFromHistory == Step(TestThirdStepType, "Payload", 5))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when requested step is not in history then exception is thrown`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))

            //when
            getStepFromHistory(TestThirdStepType)

            //then exception is thrown
        }
    }

    @Test
    fun `when multiple steps of the same type are present in history then last one is returned`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestThirdStepType, "first", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "last", 5))
            setStep(Step(TestSecondStepType))

            //when
            val stepFromHistory = getStepFromHistory(TestThirdStepType)
            print(stepFromHistory)

            //then
            assert(stepFromHistory == Step(TestThirdStepType, "last", 5))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when history is disabled then exception is thrown`() {
        runTest {
            //given
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            getStepFromHistory = GetStepFromHistoryUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestThirdStepType, "first", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "last", 5))
            setStep(Step(TestSecondStepType))

            //when
            getStepFromHistory(TestThirdStepType)

            //then exception is thrown
        }
    }
}