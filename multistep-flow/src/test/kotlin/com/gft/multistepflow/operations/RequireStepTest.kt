package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.operations.RequireStepTest.TestStepType.TestFirstStepType
import com.gft.multistepflow.operations.RequireStepTest.TestStepType.TestFourthStepType
import com.gft.multistepflow.operations.RequireStepTest.TestStepType.TestSecondStepType
import com.gft.multistepflow.operations.RequireStepTest.TestStepType.TestThirdStepType
import com.gft.multistepflow.performAction
import com.gft.multistepflow.requireStep
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.asyncUndispatchedOnUnconfinedDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class RequireStepTest {
    interface Cancellable

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        data object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestSecondStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
        data object TestThirdStepType : TestStepType<String, Int, Boolean, DefaultNoOpValidator>, Cancellable
        data object TestFourthStepType : TestStepType<String, Double, Boolean, DefaultNoOpValidator>, Cancellable
    }

    private object NotRelatedTestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class NotRelatedTestFlow : MultiStepFlow<NotRelatedTestStep>(historyEnabled = false)

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>

    private class CancelAction : Action<Cancellable, TestStepType<*, *, *, *>>() {
        override suspend fun perform(flow: MultiStepFlow<TestStepType<*, *, *, *>>, transactionId: String) {}

    }

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
    }

    private fun genericsTest() = runBlocking {
        val result = testFlow.requireStep(TestThirdStepType, TestFourthStepType)
        asyncUndispatchedOnUnconfinedDispatcher {
            result.performAction(CancelAction())
        }
    }

    @Test
    fun `when specific current step type is requested and it is current step then it is returned`() = runBlocking {
        //given
        val step = Step(TestFirstStepType)
        testFlow.start(step)
        step.performAction(object : Action<Any, TestStepType<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<TestStepType<*, *, *, *>>, transactionId: String) {
                flow.setStep(Step(TestSecondStepType))
            }
        })

        //when
        val currentStep = testFlow.requireStep(TestSecondStepType)

        //then
        assert(currentStep == Step(TestSecondStepType))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when specific current step type is requested and it is not current step then exception is thrown`() = runBlocking {

        //given
        val step = Step(TestFirstStepType)
        testFlow.start(step)
        step.performAction(object : Action<Any, TestStepType<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<TestStepType<*, *, *, *>>, transactionId: String) {
                flow.setStep(Step(TestSecondStepType))
            }
        })

        //when
        val currentStep = testFlow.requireStep(TestFirstStepType)

        //then exception is thrown
    }

    @Test
    fun `when multiple step types are requested and one of them is current step then it is returned`() = runBlocking {
        //given
        val step = Step(TestFirstStepType)
        testFlow.start(step)
        step.performAction(object : Action<Any, TestStepType<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<TestStepType<*, *, *, *>>, transactionId: String) {
                flow.setStep(Step(TestSecondStepType))
                flow.setStep(Step(TestThirdStepType, "payload", 5, true))
            }
        })

        //when
        val currentStep = testFlow.requireStep(TestThirdStepType, TestFourthStepType)

        //then
        assert(currentStep == Step(TestThirdStepType, "payload", 5, true))
    }
}