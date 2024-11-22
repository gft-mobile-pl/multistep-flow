package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.clear
import com.gft.multistepflow.operations.PaymentStep.PaymentWithQRCodeStep
import com.gft.multistepflow.performAction
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

class StartFlowTest {
    sealed interface TestFlowStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private data object TestStep : TestFlowStep
    private data object NextTestStep : TestFlowStep
    private class TestFlow : MultiStepFlow<TestFlowStep>(historyEnabled = false)

    @Test
    fun genericsTest() = runBlocking {
        val provideUserDataStep = Step(ProvideUserData, "", Unit)
        val provideCardDataStep = Step(ProvideCardData, 5, Unit)
        val introductionToQRCodeStep = Step(IntroductionToQRCode, true, Unit)
        val scarQRCode = Step(ScanQRCode)

        val payWithCardFlow = PaymentWithCardFlow()
        payWithCardFlow.start(provideUserDataStep)
        payWithCardFlow.start(provideCardDataStep)
//        payWithCardFlow.start(introductionToQRCodeStep) // compilation error: PASSED
//        payWithCardFlow.start(scarQRCode) // compilation error: PASSED

        val payWithQRCodeFlow = PaymentWithQRCodeFlow()
        payWithQRCodeFlow.start(provideUserDataStep)
//        payWithQRCodeFlow.start(provideCardDataStep) // compilation error: PASSED
        payWithQRCodeFlow.start(introductionToQRCodeStep)
        payWithQRCodeFlow.start(scarQRCode)

        @Suppress("UNUSED_VARIABLE")
        val action = object : Action<Any, PaymentWithQRCodeStep<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<PaymentWithQRCodeStep<*, *, *, *>>, transactionId: String) {
//                flow.start(scarQRCode)  // compilation error: PASSED
                payWithCardFlow.start(provideUserDataStep) // no error: PASSED
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
                testStep1.performAction(object : Action<Any, TestFlowStep>() {
                    override suspend fun perform(flow: MultiStepFlow<TestFlowStep>, transactionId: String) {
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