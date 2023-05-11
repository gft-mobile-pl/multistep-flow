package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.AwaitStepUseCaseTest.TestStepType.*
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AwaitStepUseCaseTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var awaitStep: AwaitStepUseCase

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
        startMultiStepFlow = StartMultiStepFlow(testFlow)
        awaitStep = AwaitStepUseCase(testFlow)
        setStep = SetStepUseCase(testFlow)
    }

    @Test
    fun `when requested step is reached then it is returned`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            var step: Step<*, *, *, *, *>? = null

            //when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                step = awaitStep(TestSecondStepType)
            }

            //then
            setStep(Step(TestSecondStepType))
            assert(step == Step(TestSecondStepType))
        }
    }

    @Test
    fun `when requested step is not reached then it is suspended`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            var step: Step<*, *, *, *, *>? = null

            //when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                step = awaitStep(TestSecondStepType)
                assert(false)
            }

            //then
            testScheduler.runCurrent()
            assert(step == null)
        }
    }
}