package com.gft.multistepflow.utils

import com.gft.multistepflow.model.FlowState
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.SetStepUseCase
import com.gft.multistepflow.usecases.StartMultiStepFlowUseCase
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.*
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class StepsFilteringTest {

    data class TestUserInput(val text: String)

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
        object TestFourthStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        object TestFifthStepType : TestStepType<String, TestUserInput, Unit, DefaultNoOpValidator>
        object TestSixthStepType : TestStepType<String, TestUserInput, Unit, DefaultNoOpValidator>
        object TestSeventhStepType : TestStepType<Unit, Double, Boolean, DefaultNoOpValidator>
        object TestEighthStepType : TestStepType<Unit, Int, Boolean, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var setStep: SetStepUseCase<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlowUseCase<TestStepType<*, *, *, *>>

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
        startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
        setStep = SetStepUseCase(testFlow)
    }

    @Test
    fun `when filtered step is not emitted then then result flow is empty`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestSecondStepType)
                    .toList(collectedSteps)
            }
            startMultiStepFlow(Step(TestFirstStepType))

            //then
            assert(collectedSteps.isEmpty())
        }
    }

    @Test
    fun `when filtered step is emitted then then result flow contains that step`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestFirstStepType)
                    .toList(collectedSteps)
            }
            startMultiStepFlow(Step(TestFirstStepType))

            //then
            assert(collectedSteps == listOf(Step(TestFirstStepType)))
        }
    }

    @Test
    fun `when multiple steps are emitted then result flow contains only requested step`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestSecondStepType)
                    .toList(collectedSteps)
            }
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestFourthStepType))

            //then
            assert(collectedSteps == listOf(Step(TestSecondStepType)))
        }
    }

    @Test
    fun `when filtered step was set multiple times then result flow contains all instances`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestSecondStepType)
                    .toList(collectedSteps)
            }
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestFourthStepType))

            //then
            assert(
                collectedSteps == listOf(
                    Step(TestSecondStepType),
                    Step(TestSecondStepType),
                )
            )
        }
    }

    @Test
    fun `when multiple steps are requested then result flow contains all instances`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestFirstStepType, TestSecondStepType)
                    .toList(collectedSteps)
            }
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "test", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestFourthStepType))

            //then
            assert(
                collectedSteps == listOf(
                    Step(TestFirstStepType),
                    Step(TestSecondStepType),
                    Step(TestSecondStepType),
                )
            )
        }
    }

    @Test
    fun `when requested steps have common user input type then this type is available`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<out TestStepType<*, out TestUserInput, *, *>, *, out TestUserInput, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestFifthStepType, TestSixthStepType)
                    .toList(collectedSteps) //this test would fail to compile if the type integrity will not be assured
            }
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "payload-1", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestFourthStepType))
            setStep(Step(TestFifthStepType, "payload-2", TestUserInput("user-input-1")))
            setStep(Step(TestSixthStepType, "payload-3", TestUserInput("user-input-2")))

            //then
            collectedSteps.forEach { step ->
                assert(step.userInput.text.isNotBlank()) //this test would fail to compile if the type integrity was not assured
            }
            assert(
                collectedSteps == listOf(
                    Step(TestFifthStepType, "payload-2", TestUserInput("user-input-1")),
                    Step(TestSixthStepType, "payload-3", TestUserInput("user-input-2")),
                )
            )
        }
    }

    @Test
    fun `when requested steps have common payload type then this type is available`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<out TestStepType<out String, *, *, *>, out String, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestFifthStepType, TestSixthStepType)
                    .toList(collectedSteps) //this test would fail to compile if the type integrity will not be assured
            }
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "payload-1", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestFourthStepType))
            setStep(Step(TestFifthStepType, "payload-2", TestUserInput("")))
            setStep(Step(TestSixthStepType, "payload-3", TestUserInput("")))

            //then
            collectedSteps.forEach { step ->
                assert(step.payload.isNotBlank()) //this test would fail to compile if the type integrity will not be assured
            }
            assert(
                collectedSteps == listOf(
                    Step(TestFifthStepType, "payload-2", TestUserInput("")),
                    Step(TestSixthStepType, "payload-3", TestUserInput("")),
                )
            )
        }
    }

    @Test
    fun `when requested steps have common validation type then this type is available`() {
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .filterByStepType(TestSeventhStepType, TestEighthStepType)
                    .toList(collectedSteps) //this test would fail to compile if the type integrity will not be assured
            }
            startMultiStepFlow(Step(TestSeventhStepType, Unit, 5.0, false))
            setStep(Step(TestEighthStepType, Unit, 10, false))
            setStep(Step(TestThirdStepType, "payload-1", 5))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestFourthStepType))
            setStep(Step(TestFifthStepType, "payload-2", TestUserInput("")))
            setStep(Step(TestSixthStepType, "payload-3", TestUserInput("")))

            assert(
                collectedSteps == listOf(
                    Step(TestSeventhStepType, Unit, 5.0, false),
                    Step(TestEighthStepType, Unit, 10, false)
                )
            )
        }
    }

    @Test
    fun `when flow state is filtered then only flow states with matching step types are emitted`() {
        runTest {
            // when
            val collectedFlowStates = mutableListOf<FlowState<*, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .filterNotNull()
                    .filterByStepType(TestThirdStepType)
                    .toList(collectedFlowStates) //this test would fail to compile if the type integrity will not be assured
            }
            startMultiStepFlow(Step(TestFirstStepType))
            setStep(Step(TestSecondStepType))
            setStep(Step(TestThirdStepType, "payload", 5))
            setStep(Step(TestFourthStepType))

            assert(
                collectedFlowStates == listOf(
                    FlowState(
                        currentStep = Step(TestThirdStepType, "payload", 5),
                        isAnyOperationInProgress = false,
                        stepsHistory = listOf(
                            Step(TestFirstStepType),
                            Step(TestSecondStepType),
                            Step(TestThirdStepType, "payload", 5)
                        )
                    )

                )
            )
        }
    }
}
