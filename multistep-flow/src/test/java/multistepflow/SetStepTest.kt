package multistepflow

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.SetStepUseCase
import com.gft.multistepflow.usecases.StartMultiStepFlow
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class SetStepTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
        object TestFourthStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlow<TestStepType<*, *, *, *>>

    @Test
    fun `when the flow is started and history is disabled then history is empty`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestFirstStepType)
            assert(testFlow.session.requireData().currentStep.payload == Unit)
            assert(testFlow.session.requireData().currentStep.userInput == Unit)
            assert(testFlow.session.requireData().previousSteps.isEmpty())
        }
    }

    @Test
    fun `when the flow is started and history is enabled then history is not empty and starting step is in history`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestFirstStepType)
            assert(testFlow.session.requireData().currentStep.payload == Unit)
            assert(testFlow.session.requireData().currentStep.userInput == Unit)
            assert(testFlow.session.requireData().previousSteps.size == 1)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestFirstStepType)
        }
    }

    @Test
    fun `when the history is disabled and new step is set then history is empty`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestSecondStepType)
            assert(testFlow.session.requireData().currentStep.payload == Unit)
            assert(testFlow.session.requireData().currentStep.userInput == Unit)
            assert(testFlow.session.requireData().previousSteps.isEmpty())
        }
    }

    @Test
    fun `when the history is enabled and new step is set then history is not empty and contains previous step`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestSecondStepType)
            assert(testFlow.session.requireData().currentStep.payload == Unit)
            assert(testFlow.session.requireData().currentStep.userInput == Unit)
            assert(testFlow.session.requireData().previousSteps.size == 2)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestFirstStepType)
            assert(testFlow.session.requireData().previousSteps[1].type == TestStepType.TestSecondStepType)
        }
    }

    @Test
    fun `when history is disabled and new step is set then history is empty`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(Step(TestStepType.TestThirdStepType, "test", 5))

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "test")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.isEmpty())
        }
    }

    @Test
    fun `when history is enabled and new step is set then history has all steps`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(Step(TestStepType.TestThirdStepType, "test", 5))

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "test")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.size == 3)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestFirstStepType)
            assert(testFlow.session.requireData().previousSteps[1].type == TestStepType.TestSecondStepType)
            assert(testFlow.session.requireData().previousSteps[2].type == TestStepType.TestThirdStepType)
        }
    }

    @Test
    fun `when history is disabled and same step is set then history is empty`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = false)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(Step(TestStepType.TestThirdStepType, "test", 5))
            setStep(Step(TestStepType.TestThirdStepType, "updated", 10))

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.isEmpty())
        }
    }

    @Test
    fun `when history is enabled and same step is set without reusing user input then step is replaced`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(Step(TestStepType.TestThirdStepType, "test", 5))
            setStep(Step(TestStepType.TestThirdStepType, "updated", 10), reuseUserInput = false)

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated")
            assert(testFlow.session.requireData().currentStep.userInput == 10)
            assert(testFlow.session.requireData().previousSteps.size == 3)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestFirstStepType)
            assert(testFlow.session.requireData().previousSteps[1].type == TestStepType.TestSecondStepType)
            assert(testFlow.session.requireData().previousSteps[2].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[2].payload == "updated")
            assert(testFlow.session.requireData().previousSteps[2].userInput == 10)
        }
    }

    @Test
    fun `when history is enabled and same step is set and reuse input is requested then step is replaced with previous user input`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(Step(TestStepType.TestThirdStepType, "test", 5))
            setStep(Step(TestStepType.TestThirdStepType, "updated", 10), reuseUserInput = true)

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.size == 3)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestFirstStepType)
            assert(testFlow.session.requireData().previousSteps[1].type == TestStepType.TestSecondStepType)
            assert(testFlow.session.requireData().previousSteps[2].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[2].payload == "updated")
            assert(testFlow.session.requireData().previousSteps[2].userInput == 5)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when history is enabled and new step is set and history pop is requested to step that is not in history then error is thrown`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(
                Step(TestStepType.TestThirdStepType, "test", 5),
                clearHistoryTo = TestStepType.TestFourthStepType,
                clearHistoryInclusive = false
            )

            //then exception is thrown
        }
    }

    @Test
    fun `when new step is set and history clear is requested to step that is in history then history is popped`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(
                Step(TestStepType.TestThirdStepType, "test", 5),
                clearHistoryTo = TestStepType.TestFirstStepType,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "test")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.size == 2)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestFirstStepType)
            assert(testFlow.session.requireData().previousSteps[1].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[1].payload == "test")
            assert(testFlow.session.requireData().previousSteps[1].userInput == 5)
        }
    }

    @Test
    fun `when new step is set and history clear is requested with inclusive set then history is popped inclusively`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(
                Step(TestStepType.TestThirdStepType, "test", 5),
                clearHistoryTo = TestStepType.TestFirstStepType,
                clearHistoryInclusive = true
            )

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "test")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.size == 1)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[0].payload == "test")
            assert(testFlow.session.requireData().previousSteps[0].userInput == 5)
        }
    }

    @Test
    fun `when new step is set and history clear is requested to the same type then history is popped and step is replaced`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestThirdStepType, "test", 5))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(
                Step(TestStepType.TestThirdStepType, "updated", 10),
                reuseUserInput = true,
                clearHistoryTo = TestStepType.TestThirdStepType,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.size == 1)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[0].payload == "updated")
            assert(testFlow.session.requireData().previousSteps[0].userInput == 5)
        }
    }

    @Test
    fun `when new step is set and history clear is requested to the same type with inclusive set then history is popped and step is added to history`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestThirdStepType, "test", 5))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(
                Step(TestStepType.TestThirdStepType, "updated", 10),
                clearHistoryTo = TestStepType.TestThirdStepType,
                clearHistoryInclusive = true
            )

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated")
            assert(testFlow.session.requireData().currentStep.userInput == 10)
            assert(testFlow.session.requireData().previousSteps.size == 1)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[0].payload == "updated")
            assert(testFlow.session.requireData().previousSteps[0].userInput == 10)
        }
    }

    @Test
    fun `when new step is set and history clear is requested to the same type with and reuse input is set to false then history is popped and step is added to history`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            startMultiStepFlow(Step(TestStepType.TestThirdStepType, "test", 5))
            setStep(Step(TestStepType.TestSecondStepType))
            setStep(
                Step(TestStepType.TestThirdStepType, "updated", 10),
                reuseUserInput = false,
                clearHistoryTo = TestStepType.TestThirdStepType,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated")
            assert(testFlow.session.requireData().currentStep.userInput == 10)
            assert(testFlow.session.requireData().previousSteps.size == 1)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[0].payload == "updated")
            assert(testFlow.session.requireData().previousSteps[0].userInput == 10)
        }
    }

    @Test
    fun `when new step is set and history clear is requested to the specific step then history is popped and step is added to history`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            val startingStep = Step(TestStepType.TestThirdStepType, "test", 5)
            startMultiStepFlow(startingStep)
            //then
            assert(testFlow.session.requireData().previousSteps.size == 1)

            //when
            setStep(Step(TestStepType.TestSecondStepType))
            //then
            assert(testFlow.session.requireData().previousSteps.size == 2)

            //when
            setStep(Step(TestStepType.TestThirdStepType, "updated-1", 10))
            //then
            assert(testFlow.session.requireData().previousSteps.size == 3)

            //when
            setStep(
                Step(TestStepType.TestThirdStepType, "updated-2", 15),
                reuseUserInput = false,
                clearHistoryTo = startingStep,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated-2")
            assert(testFlow.session.requireData().currentStep.userInput == 15)
            assert(testFlow.session.requireData().previousSteps.size == 1)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[0].payload == "updated-2")
            assert(testFlow.session.requireData().previousSteps[0].userInput == 15)
        }
    }

    @Test
    fun `when history clear is requested to the specific step with reuse input then history is popped and step is added to history`() {
        runBlocking {
            //given
            testFlow = MultiStepFlow(historyEnabled = true)
            startMultiStepFlow = StartMultiStepFlow(testFlow)
            setStep = SetStepUseCase(testFlow)

            //when
            val startingStep = Step(TestStepType.TestThirdStepType, "test", 5)
            startMultiStepFlow(startingStep)
            //then
            assert(testFlow.session.requireData().previousSteps.size == 1)

            //when
            setStep(Step(TestStepType.TestSecondStepType))
            //then
            assert(testFlow.session.requireData().previousSteps.size == 2)

            //when
            setStep(Step(TestStepType.TestThirdStepType, "updated-1", 10))
            //then
            assert(testFlow.session.requireData().previousSteps.size == 3)

            //when
            setStep(
                Step(TestStepType.TestThirdStepType, "updated-2", 15),
                clearHistoryTo = startingStep,
                clearHistoryInclusive = false
            )

            //then
            assert(testFlow.session.requireData().currentStep.type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().currentStep.payload == "updated-2")
            assert(testFlow.session.requireData().currentStep.userInput == 5)
            assert(testFlow.session.requireData().previousSteps.size == 1)
            assert(testFlow.session.requireData().previousSteps[0].type == TestStepType.TestThirdStepType)
            assert(testFlow.session.requireData().previousSteps[0].payload == "updated-2")
            assert(testFlow.session.requireData().previousSteps[0].userInput == 5)
        }
    }
}