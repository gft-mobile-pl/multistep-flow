package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.GoBackToStepUseCaseTest.TestStepType.*
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GoBackToStepUseCaseTest {

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlowUseCase<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var goBackToStep: GoBackToStepUseCase<TestStepType<*, *, *, *>>

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
        startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
        setStep = SetStepUseCase(testFlow)
        goBackToStep = GoBackToStepUseCase(testFlow)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when requested step type is not in history then exception is thrown`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))

            //when
            goBackToStep(TestThirdStepType)

            //then exception is thrown
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when requested step is not in history then exception is thrown`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))

            //when
            val step = Step(TestThirdStepType, "test", 5) as Step<TestStepType<*, *, *, *>, *, *, *, *>
            goBackToStep(step)

            //then exception is thrown
        }
    }

    @Test
    fun `when requested step type is in history then flow is taken back to that step`() {
        runTest {
            //given
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestThirdStepType, "payload", 5))
            setStep(Step(TestSecondStepType))

            //when
            goBackToStep(TestThirdStepType)

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
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "payload", 5))

            //when
            goBackToStep(TestThirdStepType)

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
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "payload-1", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "payload-2", 10))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "payload-3", 15))
            setStep(Step(TestSecondStepType))

            //when
            goBackToStep(TestThirdStepType)

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
