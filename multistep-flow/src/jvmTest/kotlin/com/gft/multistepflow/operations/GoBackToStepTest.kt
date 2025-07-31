package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.operations.GoBackToStepUseCaseTest.TestStepType.TestFirstStepType
import com.gft.multistepflow.operations.GoBackToStepUseCaseTest.TestStepType.TestSecondStepType
import com.gft.multistepflow.operations.GoBackToStepUseCaseTest.TestStepType.TestThirdStepType
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.unwrapNotActionErrorException
import kotlinx.coroutines.test.runTest
import net.bytebuddy.implementation.bytecode.Throw
import org.junit.Before
import org.junit.Test

internal class GoBackToStepUseCaseTest {

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

    @Test(expected = IllegalArgumentException::class)
    fun `when requested step type is not in history then exception is thrown`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            try {
                //when
                testFlow.performInActionScope { testFlow.goBackToStep(TestThirdStepType) }
            } catch (error: Throwable) {
                //then exception is thrown
                throw error.unwrapNotActionErrorException()
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when requested step is not in history then exception is thrown`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            //when
            val step = Step(TestThirdStepType, "test", 5) as Step<TestStepType<*, *, *, *>, *, *, *, *>
            try {
                testFlow.performInActionScope { testFlow.goBackToStep(step) }
            } catch (error: Throwable) {
                //then exception is thrown
                throw error.unwrapNotActionErrorException()
            }
        }
    }

    @Test
    fun `when requested step type is in history then flow is taken back to that step`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            //when
            testFlow.performInActionScope { testFlow.goBackToStep(TestThirdStepType) }

            //then
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestThirdStepType, "payload", 5),
                )
            )
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "payload", 5))
        }
    }

    @Test
    fun `when requested step type is last in history then flow is unchanged`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload", 5)) }

            //when
            testFlow.performInActionScope { testFlow.goBackToStep(TestThirdStepType) }

            //then
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "payload", 5),
                )
            )
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "payload", 5))
        }
    }

    @Test
    fun `when there are multiple steps of the requested type then history is rolled back to the last step of that type`() {
        runTest {
            //given
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload-1", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload-2", 10)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload-3", 15)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }

            //when
            testFlow.performInActionScope { testFlow.goBackToStep(TestThirdStepType) }

            //then
            assert(
                testFlow.session.requireData().stepsHistory == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "payload-1", 5),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "payload-2", 10),
                    Step(TestSecondStepType),
                    Step(TestThirdStepType, "payload-3", 15),
                )
            )
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "payload-3", 15))
        }
    }
}
