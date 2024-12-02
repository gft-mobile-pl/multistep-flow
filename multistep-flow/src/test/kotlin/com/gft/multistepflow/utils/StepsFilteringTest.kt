package com.gft.multistepflow.utils

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.FlowState
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestEighthStepType
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestFifthStepType
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestFirstStepType
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestFourthStepType
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestSecondStepType
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestSeventhStepType
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestSixthStepType
import com.gft.multistepflow.utils.StepsFilteringTest.TestStepType.TestThirdStepType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class StepsFilteringTest {

    data class TestUserInput(val text: String)

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        data object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestThirdStepType : TestStepType<String, Int, Unit, DefaultNoOpValidator>
        data object TestFourthStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestFifthStepType : TestStepType<String, TestUserInput, Unit, DefaultNoOpValidator>
        data object TestSixthStepType : TestStepType<String, TestUserInput, Unit, DefaultNoOpValidator>
        data object TestSeventhStepType : TestStepType<Unit, Double, Boolean, DefaultNoOpValidator>
        data object TestEighthStepType : TestStepType<Unit, Int, Boolean, DefaultNoOpValidator>
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
        testFlow = TestFlow(true)
    }

    @Test
    fun `when filtered step is not emitted then then result flow is empty`() = runTest {
        // when
        val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .mapNotNull { it?.currentStep }
                .distinctUntilChanged()
                .filterByStepType(TestSecondStepType)
                .toList(collectedSteps)
        }
        testFlow.start(Step(TestFirstStepType))

        //then
        assert(collectedSteps.isEmpty())
    }

    @Test
    fun `when filtered step is emitted then then result flow contains that step`() = runTest {
        // when
        val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .mapNotNull { it?.currentStep }
                .distinctUntilChanged()
                .filterByStepType(TestFirstStepType)
                .toList(collectedSteps)
        }
        testFlow.start(Step(TestFirstStepType))

        //then
        assert(collectedSteps == listOf(Step(TestFirstStepType)))
    }

    @Test
    fun `when multiple steps are emitted then result flow contains only requested step`() =
        runTest {
            // when
            val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                testFlow.session.data
                    .mapNotNull { it?.currentStep }
                    .distinctUntilChanged()
                    .filterByStepType(TestSecondStepType)
                    .map {
                        println("#Test GOT $it")
                        it
                    }
                    .toList(collectedSteps)
            }
            testFlow.start(Step(TestFirstStepType))
            testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "test", 5)) }
            testFlow.performInActionScope { testFlow.setStep(Step(TestFourthStepType)) }

            //then
            assert(collectedSteps == listOf(Step(TestSecondStepType)))
        }

    @Test
    fun `when filtered step was set multiple times then result flow contains all instances`() = runTest {
        // when
        val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .mapNotNull { it?.currentStep }
                .distinctUntilChanged()
                .filterByStepType(TestSecondStepType)
                .toList(collectedSteps)
        }
        testFlow.start(Step(TestFirstStepType))
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "test", 5)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFourthStepType)) }

        //then
        assert(
            collectedSteps == listOf(
                Step(TestSecondStepType),
                Step(TestSecondStepType),
            )
        )
    }

    @Test
    fun `when multiple steps are requested then result flow contains all instances`() = runTest {
        // when
        val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .mapNotNull { it?.currentStep }
                .distinctUntilChanged()
                .filterByStepType(TestFirstStepType, TestSecondStepType)
                .toList(collectedSteps)
        }
        testFlow.start(Step(TestFirstStepType))
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "test", 5)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFourthStepType)) }

        //then
        assert(
            collectedSteps == listOf(
                Step(TestFirstStepType),
                Step(TestSecondStepType),
                Step(TestSecondStepType),
            )
        )
    }

    @Test
    fun `when requested steps have common user input type then this type is available`() = runTest {
        // when
        val collectedSteps = mutableListOf<Step<out TestStepType<*, out TestUserInput, *, *>, *, out TestUserInput, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .mapNotNull { it?.currentStep }
                .distinctUntilChanged()
                .filterByStepType(TestFifthStepType, TestSixthStepType)
                .toList(collectedSteps) //this test would fail to compile if the type integrity will not be assured
        }
        testFlow.start(Step(TestFirstStepType))
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload-1", 5)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFourthStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFifthStepType, "payload-2", TestUserInput("user-input-1"))) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSixthStepType, "payload-3", TestUserInput("user-input-2"))) }

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

    @Test
    fun `when requested steps have common payload type then this type is available`() = runTest {
        // when
        val collectedSteps = mutableListOf<Step<out TestStepType<out String, *, *, *>, out String, *, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .mapNotNull { it?.currentStep }
                .distinctUntilChanged()
                .filterByStepType(TestFifthStepType, TestSixthStepType)
                .toList(collectedSteps) //this test would fail to compile if the type integrity will not be assured
        }
        testFlow.start(Step(TestFirstStepType))
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload-1", 5)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFourthStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFifthStepType, "payload-2", TestUserInput(""))) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSixthStepType, "payload-3", TestUserInput(""))) }

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

    @Test
    fun `when requested steps have common validation type then this type is available`() = runTest {
        // when
        val collectedSteps = mutableListOf<Step<*, *, *, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .mapNotNull { it?.currentStep }
                .distinctUntilChanged()
                .filterByStepType(TestSeventhStepType, TestEighthStepType)
                .toList(collectedSteps) //this test would fail to compile if the type integrity will not be assured
        }
        testFlow.start(Step(TestSeventhStepType, Unit, 5.0, false))
        testFlow.performInActionScope { testFlow.setStep(Step(TestEighthStepType, Unit, 10, false)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload-1", 5)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFourthStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFifthStepType, "payload-2", TestUserInput(""))) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestSixthStepType, "payload-3", TestUserInput(""))) }

        assert(
            collectedSteps == listOf(
                Step(TestSeventhStepType, Unit, 5.0, false),
                Step(TestEighthStepType, Unit, 10, false)
            )
        )
    }

    @Test
    fun `when flow state is filtered then only flow states with matching step types are emitted`() = runTest {
        // when
        val collectedFlowStates = mutableListOf<FlowState<*, *, *, *>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            testFlow.session.data
                .filterNotNull()
                .filterByStepType(TestThirdStepType)
                .distinctUntilChangedBy { flowState -> flowState.currentStep }
                .toList(collectedFlowStates) //this test would fail to compile if the type integrity will not be assured
        }
        testFlow.start(Step(TestFirstStepType))
        testFlow.performInActionScope { testFlow.setStep(Step(TestSecondStepType)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestThirdStepType, "payload", 5)) }
        testFlow.performInActionScope { testFlow.setStep(Step(TestFourthStepType)) }

        assertEquals(Step(TestThirdStepType, "payload", 5), collectedFlowStates.first().currentStep)
        assertEquals(
            listOf(
                Step(TestFirstStepType),
                Step(TestSecondStepType),
                Step(TestThirdStepType, "payload", 5)
            ),
            collectedFlowStates.first().stepsHistory
        )

    }
}
