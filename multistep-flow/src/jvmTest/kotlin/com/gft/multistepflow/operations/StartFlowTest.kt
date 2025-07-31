package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.UserInputValidator
import com.gft.multistepflow.clear
import com.gft.multistepflow.operations.PaymentStep.PaymentWithQRCodeStep
import com.gft.multistepflow.performAction
import com.gft.multistepflow.requireState
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.asyncUndispatchedOnUnconfinedDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val VALID_USER_INPUT = "some correct input"
private const val INVALID_USER_INPUT = "some incorrect input"

class StartFlowTest {
    sealed interface TestFlowStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator>

    private data object TestStep : TestFlowStep<Unit, Unit, Unit, DefaultNoOpValidator>
    private data object NextTestStep : TestFlowStep<Unit, Unit, Unit, DefaultNoOpValidator>
    private data object StepToValidate : TestFlowStep<Unit, String, Boolean, TestValidator>
    private data object AnotherStepToValidate : TestFlowStep<Unit, String, Boolean, TestValidator>

    private class TestFlow(historyEnabled: Boolean = false) : MultiStepFlow<TestFlowStep<*, *, *, *>>(historyEnabled = historyEnabled)
    private class TestValidator : UserInputValidator<String, Boolean, TestFlowStep<*, *, *, *>>() {
        override fun validate(flow: MultiStepFlow<TestFlowStep<*, *, *, *>>, currentUserInput: String, newUserInput: String, currentValidationResult: Boolean): Boolean {
            return newUserInput == VALID_USER_INPUT
        }
    }

    @Test
    fun genericsTest() = runBlocking {
        val provideUserDataStep1 = Step(ProvideUserData, "", Unit)
        val provideUserDataStep2 = Step(ProvideUserData, "", Unit)
        val provideUserDataStep3 = Step(ProvideUserData, "", Unit)
        val provideCardDataStep = Step(ProvideCardData, 5, Unit)
        val introductionToQRCodeStep = Step(IntroductionToQRCode, true, Unit)
        val scarQRCode = Step(ScanQRCode)

        val payWithCardFlow = PaymentWithCardFlow()
        payWithCardFlow.start(provideUserDataStep1)
        payWithCardFlow.start(provideCardDataStep)
//        payWithCardFlow.start(introductionToQRCodeStep) // compilation error: PASSED
//        payWithCardFlow.start(scarQRCode) // compilation error: PASSED

        val payWithQRCodeFlow = PaymentWithQRCodeFlow()
        payWithQRCodeFlow.start(provideUserDataStep2)
//        payWithQRCodeFlow.start(provideCardDataStep) // compilation error: PASSED
        payWithQRCodeFlow.start(introductionToQRCodeStep)
        payWithQRCodeFlow.start(scarQRCode)

        @Suppress("UNUSED_VARIABLE")
        val action = object : Action<Any, PaymentWithQRCodeStep<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<PaymentWithQRCodeStep<*, *, *, *>>, transactionId: String) {
//                flow.start(scarQRCode)  // compilation error: PASSED
                payWithCardFlow.start(provideUserDataStep3) // no error: PASSED
            }
        }
    }

    @Test
    fun `given flow is not initialized, when MultiStepFlow_start is invoked, start the flow and set initial step`() =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow()

            val result = testFlow.start(testStep)

            assertTrue(result.isSuccess)
            assertTrue(testFlow.lifecycle.value is Lifecycle.State.Started)
            assertEquals(testStep, testFlow.session.data.value?.currentStep)
        }

    @Test
    fun `given validation is required, when MultiStepFlow_start is invoked, the initial step should be validated`() =
        runBlocking {
            val testFlow1 = TestFlow()
            val testFlow2 = TestFlow()
            val invalidTestStep = Step(
                type = StepToValidate,
                payload = Unit,
                userInput = INVALID_USER_INPUT,
                validationResult = false,
                validator = TestValidator()
            )
            val validTestStep = Step(
                type = StepToValidate,
                payload = Unit,
                userInput = VALID_USER_INPUT,
                validationResult = false,
                validator = TestValidator()
            )

            testFlow1.start(
                initialStep = invalidTestStep,
                validateUserInput = true
            )

            testFlow2.start(
                initialStep = validTestStep,
                validateUserInput = true
            )

            assertEquals(testFlow1.requireState().currentStep.validationResult, false)
            assertEquals(testFlow2.requireState().currentStep.validationResult, true)
        }

    @Test
    fun `given validation is required, when MultiStepFlow_start is invoked, the initial steps history should be validated`() =
        runBlocking {
            val testFlow = TestFlow(historyEnabled = true)
            val stepsHistory = listOf(
                Step(
                    type = StepToValidate,
                    payload = Unit,
                    userInput = VALID_USER_INPUT,
                    validationResult = false,
                    validator = TestValidator()
                ),
                Step(
                    type = StepToValidate,
                    payload = Unit,
                    userInput = INVALID_USER_INPUT,
                    validationResult = false,
                    validator = TestValidator()
                )
            )

            testFlow.start(
                stepsHistory = stepsHistory,
                validateUserInput = true
            )

            assertEquals(testFlow.requireState().stepsHistory[0].validationResult, true)
            assertEquals(testFlow.requireState().stepsHistory[1].validationResult, false)
            assertEquals(testFlow.requireState().currentStep.validationResult, false) // current step is last step
        }

    @Test
    fun `given flow is started, when MultiStepFlow_start is invoked, the result is Result_failure(IllegalFlowStateException) and step is not set`() =
        runBlocking {
            val testStep1 = Step(TestStep)
            val testStep2 = Step(TestStep)
            val testFlow = TestFlow()

            testFlow.start(testStep1)
            val result = testFlow.start(testStep2)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalFlowStateException)
            assertEquals(testStep1, testFlow.session.data.value?.currentStep)
        }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given flow is being cleared, when MultiStepFlow_start is invoked, await clearing before starting the flow again`() =
        runBlocking {
            val testStep1 = Step(TestStep)
            val testStep2 = Step(TestStep)
            val testFlow = TestFlow()
            val onContinueAction = Channel<Unit>()
            var result: Result<Unit>? = null

            testFlow.start(testStep1)
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep1.performAction(object : Action<Any, TestFlowStep<*, *, *, *>>() {
                    override suspend fun perform(flow: MultiStepFlow<TestFlowStep<*, *, *, *>>, transactionId: String) {
                        withContext(NonCancellable) {
                            onContinueAction.receive()
                        }
                    }
                })
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testFlow.clear()
            }

            asyncUndispatchedOnUnconfinedDispatcher {
                result = testFlow.start(testStep2)
            }

            assertNull(result)
            assertTrue(testFlow.lifecycle.value is Lifecycle.State.Clearing)
            assertEquals(testStep1, testFlow.session.data.value?.currentStep)

            onContinueAction.send(Unit)

            assertTrue(result?.isSuccess == true)
            assertTrue(testFlow.lifecycle.value is Lifecycle.State.Started)
            assertEquals(testStep1, testFlow.session.data.value?.currentStep)
        }
}