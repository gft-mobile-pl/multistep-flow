package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiFlowAction
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.operations.SetStepTest.TestStepType.TestFirstStepType
import com.gft.multistepflow.operations.SetStepTest.TestStepType.TestFourthStepType
import com.gft.multistepflow.operations.SetStepTest.TestStepType.TestSecondStepType
import com.gft.multistepflow.operations.SetStepTest.TestStepType.TestThirdStepType
import com.gft.multistepflow.operations.SetStepTest.UnrelatedTestStepType.SomeUnrelatedStepType
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.unwrapNotActionErrorException
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class SetStepTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        data object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
        data object TestFourthStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
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

    private sealed interface UnrelatedTestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        data object SomeUnrelatedStepType : UnrelatedTestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
    }

    private class UnrelatedTestFlow : MultiStepFlow<UnrelatedTestStepType<*, *, *, *>>(historyEnabled = false)

    private lateinit var testFlow: TestFlow

    @Test
    fun genericsTest() {
        object : Action<Any, TestStepType<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<TestStepType<*, *, *, *>>, transactionId: String) {
                val step = Step(TestFirstStepType)
                flow.setStep(step)

                val unrelatedStep = Step(SomeUnrelatedStepType)
                val unrelatedFlow = UnrelatedTestFlow()
//                unrelatedFlow.setStep(unrelatedStep) // compilation error: PASSED
//                flow.setStep(unrelatedStep) // compilation error: PASSED
            }
        }

        object : MultiFlowAction<CancellableStep, PaymentStep<*, *, *, *>>() {
            override suspend fun performAction(flow: MultiStepFlow<out PaymentStep<*, *, *, *>>, transactionId: String) {
//                flow.setStep(Step(ScanQRCode)) // compilation error: PASSED
//                flow.setStep(Step(ProvideCardData, 5, Unit)) // compilation error: PASSED
//                flow.setStep(Step(NotRelatedCancellableStepType)) // compilation error: PASSED

                if (flow is PaymentWithCardFlow) flow.setStep(Step(ProvideCardData, 5, Unit))
                if (flow is PaymentWithQRCodeFlow) flow.setStep(Step(ScanQRCode))
            }
        }

    }

    @Test
    fun `when set step is called on an unrelated flow, exception is throw`() {
        //given
        testFlow = TestFlow(historyEnabled = false)
    }

    @Test(expected = IllegalFlowException::class)
    fun `when the history is disabled and new step is set then history is empty`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = false)
            val unrelatedFlow = TestFlow(historyEnabled = false)

            //when
            testFlow.start(Step(TestFirstStepType))

            try {
                testFlow.performInActionScope {
                    unrelatedFlow.setStep(Step(TestSecondStepType))
                }
            } catch (error: Throwable) {
                throw error.unwrapNotActionErrorException()
            }
        }
    }

    @Test
    fun `when the history is enabled and new step is set then history is not empty and contains previous step`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestSecondStepType))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType)
                )
            )
        }
    }

    @Test
    fun `when history is enabled and new step is set then history has all steps`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "test", 5))
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "test", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "test", 5)
                )
            )
        }
    }

    @Test
    fun `when history is disabled and same step is set then history is empty`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = false)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "test", 5))
                testFlow.setStep(Step(TestThirdStepType, "updated", 10))
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(testFlow.session.requireData().stepsHistory.isEmpty())
        }
    }

    @Test
    fun `when same step is set without reusing user input then step is replaced`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "test", 5))
                testFlow.setStep(Step(TestThirdStepType, "updated", 10), reuseUserInput = false)
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "updated", 10)
                )
            )
        }
    }

    @Test
    fun `when same step is set and reuse input is requested then step is replaced with previous user input`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "test", 5))
                testFlow.setStep(Step(TestThirdStepType, "updated", 10), reuseUserInput = true)
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "updated", 5),
                )
            )
        }
    }

    @Test
    fun `when same step is set and reuse input is not specified then step is replaced with previous user input`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "test", 5))
                testFlow.setStep(Step(TestThirdStepType, "updated", 10))
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "updated", 5),
                )
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when history clear is requested to step that is not in history then error is thrown`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            try {
                testFlow.performInActionScope {
                    testFlow.setStep(Step(TestSecondStepType))
                    testFlow.setStep(
                        Step(TestThirdStepType, "test", 5),
                        clearHistoryTo = TestFourthStepType,
                        clearHistoryInclusive = false
                    )
                }
            } catch (error: Exception) {
                throw error.unwrapNotActionErrorException()
            }

            //then exception is thrown
        }
    }

    @Test
    fun `when history clear is requested to step that is in history then history is cleared`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(
                    Step(TestThirdStepType, "test", 5),
                    clearHistoryTo = TestFirstStepType,
                    clearHistoryInclusive = false
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "test", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestThirdStepType, "test", 5),
                )
            )
        }
    }

    @Test
    fun `when inclusive clear history is requested then history is cleared inclusively`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(
                    Step(TestThirdStepType, "test", 5),
                    clearHistoryTo = TestFirstStepType,
                    clearHistoryInclusive = true
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "test", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "test", 5),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested with reusing user input then history is cleared and step has previous user input`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestThirdStepType, "test", 5))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(
                    Step(TestThirdStepType, "updated", 10),
                    reuseUserInput = true,
                    clearHistoryTo = TestThirdStepType,
                    clearHistoryInclusive = false
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "updated", 5),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the same type with inclusive set then history is cleared inclusively and step is added to history`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestThirdStepType, "test", 5))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(
                    Step(TestThirdStepType, "updated", 10),
                    clearHistoryTo = TestThirdStepType,
                    clearHistoryInclusive = true
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "updated", 10),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the same type with and reuse input is set to false then history is cleared and step does not have old user input`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestThirdStepType, "test", 5))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(
                    Step(TestThirdStepType, "updated", 10),
                    reuseUserInput = false,
                    clearHistoryTo = TestThirdStepType,
                    clearHistoryInclusive = false
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "updated", 10),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the specific step then history is cleared to the specific step`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestThirdStepType, "test", 5))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "updated-1", 10))
                testFlow.setStep(
                    Step(TestThirdStepType, "updated-2", 15),
                    reuseUserInput = false,
                    clearHistoryTo = Step(TestThirdStepType, "test", 5),
                    clearHistoryInclusive = false
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated-2", 15))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "updated-2", 15),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the specific step and reuse input is requested then history is cleared to the specific step and input is transferred`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestThirdStepType, "test", 5))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "updated-1", 10))
                testFlow.setStep(
                    Step(TestThirdStepType, "updated-2", 15),
                    reuseUserInput = true,
                    clearHistoryTo = Step(TestThirdStepType, "test", 5),
                    clearHistoryInclusive = false
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated-2", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "updated-2", 5),
                )
            )
        }
    }

    @Test
    fun `when reuse input is not explicitly specified then input is transferred`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestThirdStepType, "test", 5))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(Step(TestThirdStepType, "updated-1", 10))
                testFlow.setStep(
                    Step(TestThirdStepType, "updated-2", 15),
                    clearHistoryTo = Step(TestThirdStepType, "test", 5),
                    clearHistoryInclusive = false
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated-2", 5))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "updated-2", 5),
                )
            )
        }
    }

    @Test
    fun `when inclusive history clear is requested to specific step then history is cleared to the specific step along with that step`() {
        runBlocking {
            //given
            testFlow = TestFlow(historyEnabled = true)

            //when
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope {
                testFlow.setStep(Step(TestThirdStepType, "test", 5))
                testFlow.setStep(Step(TestSecondStepType))
                testFlow.setStep(
                    Step(TestThirdStepType, "updated", 10),
                    clearHistoryTo = Step(TestFirstStepType),
                    clearHistoryInclusive = true
                )
            }

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestThirdStepType, "updated", 10),
                )
            )
        }
    }
}
