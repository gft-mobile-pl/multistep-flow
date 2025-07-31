package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.getStepFromHistory
import com.gft.multistepflow.operations.GetStepFromHistoryUseCaseTest.TestStepType.TestFirstStepType
import com.gft.multistepflow.operations.GetStepFromHistoryUseCaseTest.TestStepType.TestSecondStepType
import com.gft.multistepflow.operations.GetStepFromHistoryUseCaseTest.TestStepType.TestThirdStepType
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class GetStepFromHistoryUseCaseTest {
    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        data object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
    }

    private class TestFlow(historyEnabled: Boolean) : MultiStepFlow<TestStepType<*, *, *, *>>(historyEnabled) {
        suspend fun performInActionScope(block: suspend Action<Any, TestStepType<*, *, *, *>>.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            (session.data.value?.currentStep as? Step<TestStepType<*, *, *, *>, *, *, *, *>)?.let { step ->
                step.performAction(object : Action<Any, TestStepType<*, *, *, *>>() {
                    override suspend fun perform(flow: MultiStepFlow<TestStepType<*, *, *, *>>, transactionId: String) {
                        block()
                    }
                })
            }
        }
    }

    private lateinit var testFlow: TestFlow

    @Before
    fun setUp() {
        testFlow = TestFlow(historyEnabled = true)
    }

    @Test
    fun `when requested step is in history then it is returned`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "Payload", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            //when
            val stepFromHistory = testFlow.getStepFromHistory(TestThirdStepType)

            //then
            assert(stepFromHistory == Step(TestThirdStepType, "Payload", 5))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when requested step is not in history then exception is thrown`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            //when
            testFlow.getStepFromHistory(TestThirdStepType)

            //then exception is thrown
        }
    }

    @Test
    fun `when multiple steps of the same type are present in history then last one is returned`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "first", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "last", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            //when
            val stepFromHistory = testFlow.getStepFromHistory(TestThirdStepType)

            //then
            assert(stepFromHistory == Step(TestThirdStepType, "last", 5))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when history is disabled then exception is thrown`() {
        runTest {
            //given
            testFlow = TestFlow(historyEnabled = false)
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "first", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "last", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            //when
            testFlow.getStepFromHistory(TestThirdStepType)

            //then exception is thrown
        }
    }
}