package com.gft.multistepflow.usecases

import com.gft.multistepflow.usecases.SetStepUseCaseTest.TestStepType.*
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class SetStepUseCaseTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
        object TestFourthStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlowUseCase<TestStepType<*, *, *, *>>

    @Test
    fun `when the history is disabled and new step is set then history is empty`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestSecondStepType))
            assert(testFlow.session.requireData().previousSteps.isEmpty())
        }
    }

    @Test
    fun `when the history is enabled and new step is set then history is not empty and contains previous step`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestSecondStepType))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
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
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "test", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
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
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestThirdStepType, "updated", 10))

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(testFlow.session.requireData().previousSteps.isEmpty())
        }
    }

    @Test
    fun `when same step is set without reusing user input then step is replaced`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestThirdStepType, "updated", 10), reuseUserInput = false)

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
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
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestThirdStepType, "updated", 10), reuseUserInput = true)

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
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
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestThirdStepType, "updated", 10))

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
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
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(
                Step(TestThirdStepType, "test", 5),
                clearHistoryTo = TestFourthStepType,
                clearHistoryInclusive = false
            )

            //then exception is thrown
        }
    }

    @Test
    fun `when history clear is requested to step that is in history then history is cleared`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(
                Step(TestThirdStepType, "test", 5),
                clearHistoryTo = TestFirstStepType,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "test", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
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
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(
                Step(TestThirdStepType, "test", 5),
                clearHistoryTo = TestFirstStepType,
                clearHistoryInclusive = true
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "test", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "test", 5),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested with reusing user input then history is cleared and step has previous user input`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(
                Step(TestThirdStepType, "updated", 10),
                reuseUserInput = true,
                clearHistoryTo = TestThirdStepType,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "updated", 5),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the same type with inclusive set then history is cleared inclusively and step is added to history`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(
                Step(TestThirdStepType, "updated", 10),
                clearHistoryTo = TestThirdStepType,
                clearHistoryInclusive = true
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "updated", 10),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the same type with and reuse input is set to false then history is cleared and step does not have old user input`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(
                Step(TestThirdStepType, "updated", 10),
                reuseUserInput = false,
                clearHistoryTo = TestThirdStepType,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "updated", 10),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the specific step then history is cleared to the specific step`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "updated-1", 10))
            setStep(
                Step(TestThirdStepType, "updated-2", 15),
                reuseUserInput = false,
                clearHistoryTo = Step(TestThirdStepType, "test", 5),
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated-2", 15))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "updated-2", 15),
                )
            )
        }
    }

    @Test
    fun `when history clear is requested to the specific step and reuse input is requested then history is cleared to the specific step and input is transferred`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "updated-1", 10))
            setStep(
                Step(TestThirdStepType, "updated-2", 15),
                reuseUserInput = true,
                clearHistoryTo = Step(TestThirdStepType, "test", 5),
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated-2", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "updated-2", 5),
                )
            )
        }
    }

    @Test
    fun `when reuse input is not explicitly specified then input is transferred`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "updated-1", 10))
            setStep(
                Step(TestThirdStepType, "updated-2", 15),
                clearHistoryTo = Step(TestThirdStepType, "test", 5),
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated-2", 5))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "updated-2", 5),
                )
            )
        }
    }

    @Test
    fun `when inclusive history clear is requested to specific step then history is cleared to the specific step along with that step`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(
                Step(TestThirdStepType, "updated", 10),
                clearHistoryTo = Step(TestFirstStepType),
                clearHistoryInclusive = true
            )

            //then
            assert(testFlow.session.requireData().currentStep == Step(TestThirdStepType, "updated", 10))
            assert(
                testFlow.session.requireData().previousSteps == listOf(
                    Step(TestThirdStepType, "updated", 10),
                )
            )
        }
    }
}