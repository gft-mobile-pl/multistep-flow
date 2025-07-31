package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.awaitStep
import com.gft.multistepflow.operations.AwaitStepTest.TestStepType.TestFirstStepType
import com.gft.multistepflow.operations.AwaitStepTest.TestStepType.TestSecondStepType
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AwaitStepTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        data object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
    }

    private class TestFlow(historyEnabled: Boolean) :
        MultiStepFlow<TestStepType<*, *, *, *>>(historyEnabled) {
        suspend fun performInActionScope(block: suspend Action<Any, TestStepType<*, *, *, *>>.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            ((session.data.value?.currentStep as? Step<TestStepType<*, *, *, *>, *, *, *, *>)?.performAction?.invoke(
                object : Action<Any, TestStepType<*, *, *, *>>() {
                    override suspend fun perform(
                        flow: MultiStepFlow<TestStepType<*, *, *, *>>,
                        transactionId: String
                    ) {
                        block()
                    }
                }))
        }
    }

    private lateinit var testFlow: TestFlow

    @Before
    fun setUp() {
        testFlow = TestFlow(historyEnabled = true)
    }

    @Test
    fun `when requested step is reached then it is returned`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            var step: Step<*, *, *, *, *>? = null

            //when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                step = testFlow.awaitStep(TestSecondStepType)
            }

            //then
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            assert(step == Step(TestSecondStepType))
        }
    }

    @Test
    fun `when requested step is not reached then it is suspended`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            var step: Step<*, *, *, *, *>? = null

            //when
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                step = testFlow.awaitStep(TestSecondStepType)
                assert(false)
            }

            //then
            testScheduler.runCurrent()
            assert(step == null)
        }
    }
}