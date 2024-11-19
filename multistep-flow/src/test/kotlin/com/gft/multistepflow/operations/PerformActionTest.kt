package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiFlowAction
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.annotations.PerformActionInActionScope
import com.gft.multistepflow.operations.NotRelatedSteps.NotRelatedCancellableStepType
import com.gft.multistepflow.operations.PaymentStep.PaymentWithQRCodeStep
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.asyncUndispatchedOnUnconfinedDispatcher
import com.gft.multistepflow.utils.unwrapNotActionErrorException
import io.mockk.Runs
import io.mockk.called
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PerformActionTest {
    private object TestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class TestFlow : MultiStepFlow<TestStep>(historyEnabled = false)

    private object NotRelatedTestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class NotRelatedTestFlow : MultiStepFlow<NotRelatedTestStep>(historyEnabled = false)

    private class TestFlowAction(
        private val block: suspend TestFlowAction.(MultiStepFlow<TestStep>) -> Unit,
    ) : Action<TestStep, TestStep>() {
        override suspend fun perform(flow: MultiStepFlow<TestStep>, transactionId: String) {
            block(flow)
        }
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given outside of the Action scope, when Step_performAction is invoked multiple time, actions are enqueued`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow()
            val allActionsQueued = Channel<Unit>()
            val action1 = spyk(TestFlowAction {
                allActionsQueued.receive()
            })
            val action2 = spyk(TestFlowAction {})
            val action3 = spyk(TestFlowAction {})

            testFlow.start(testStep)
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(action1)
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(action2)
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(action3)
            }

            verify {
                action2 wasNot called
                action3 wasNot called
            }

            allActionsQueued.send(Unit)

            coVerifyOrder {
                action1.internalPerform(testFlow, any())
                action2.internalPerform(testFlow, any())
                action3.internalPerform(testFlow, any())
            }
        }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given inside the Action scope, when Step_performChildAction is invoked, action is performed immediately before queued actions`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow()
            val allActionsQueued = Channel<Unit>()
            val action1Child = spyk(TestFlowAction {})
            val action1 = spyk(TestFlowAction {
                allActionsQueued.receive()
                testStep.performChildAction(action1Child)
            })
            val action2 = spyk(TestFlowAction {})

            testFlow.start(testStep)
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(action1)
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(action2)
            }

            verify {
                action1Child wasNot called
                action2 wasNot called
            }

            allActionsQueued.send(Unit)

            coVerifyOrder {
                action1.internalPerform(testFlow, any())
                action1Child.internalPerform(testFlow, any())
                action2.internalPerform(testFlow, any())
            }
        }

    @Test(expected = InvalidFlowException::class)
    fun `given inside a scope of an Action performed in scope of unrelated flow, when Step_performChildAction is invoked, an error is thrown`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            TestFlow().apply { start(testStep) }
            val notRelatedTestStep = Step(TestStep)
            TestFlow().apply { start(notRelatedTestStep) }

            try {
                testStep.performAction(TestFlowAction {
                    notRelatedTestStep.performChildAction(TestFlowAction {})
                })
            } catch (error: Throwable) {
                throw error.unwrapNotActionErrorException()
            }
        }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given inside the Action scope, when Step_performAction is invoked on a Step of an unrelated flow, the current Action is suspended until the just started action completes`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow().apply { start(testStep) }
            val notRelatedTestStep = Step(NotRelatedTestStep)
            val notRelatedTestFlow = NotRelatedTestFlow().apply { start(notRelatedTestStep) }
            val onContinueNotRelatedStepAction = Channel<Unit>()
            val notRelatedTestStepAction = spyk(object : Action<NotRelatedTestStep, NotRelatedTestStep>() {
                override suspend fun perform(flow: MultiStepFlow<NotRelatedTestStep>, transactionId: String) {
                    onContinueNotRelatedStepAction.receive()
                }
            })

            val lastAction1Task = mockk<Runnable> { every { run() } just Runs }
            val action1 = spyk(TestFlowAction {
                notRelatedTestStep.performAction(notRelatedTestStepAction)
                lastAction1Task.run()
            })

            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(action1)
            }

            verify {
                lastAction1Task wasNot called
            }

            onContinueNotRelatedStepAction.send(Unit)

            coVerifyOrder {
                action1.internalPerform(testFlow, any())
                notRelatedTestStepAction.internalPerform(notRelatedTestFlow, any())
                lastAction1Task.run()
            }
        }

    @OptIn(PerformActionInActionScope::class)
    @Test(expected = InvalidFlowException::class)
    fun `given inside the Action scope, when Step_performAction is invoked on a Step belonging to a flow in scope of which the current Action is performed, exception is thrown`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            TestFlow().apply { start(testStep) }

            try {
                testStep.performAction(TestFlowAction {
                    testStep.performAction(TestFlowAction {}) // opt-in applied to the test method (!)
                })
            } catch (error: Throwable) {
                throw error.unwrapNotActionErrorException()
            }
        }

    // This method should never be run as a part of test suite - its purpose is to check the generics definition statically
    @Suppress("UNUSED_VARIABLE")
    fun genericsTestInsideAction() = runBlocking {
        val provideUserDataStep = Step(ProvideUserData, "", Unit)
        val provideCardDataStep = Step(ProvideCardData, 5, Unit)
        val introductionToQRCodeStep = Step(IntroductionToQRCode, true, Unit)
        val scarQRCode = Step(ScanQRCode)
        val notRelatedCancellableStep = Step(NotRelatedCancellableStepType)

        object : Action<Any, PaymentWithQRCodeStep<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<PaymentWithQRCodeStep<*, *, *, *>>, transactionId: String) {
//                provideUserDataStep.performAction(ConfirmUserDataAction())  // compilation error: PASSED
                provideCardDataStep.performAction(ConfirmCardDataAction())
//                scarQRCode.performAction(ScanQRCodeAction()) // compilation error: PASSED
                notRelatedCancellableStep.performAction(NotRelatedAction())
            }
        }

        object : MultiFlowAction<Any, PaymentStep<*, *, *, *>>() {
            override suspend fun performAction(flow: MultiStepFlow<out PaymentStep<*, *, *, *>>, transactionId: String) {
//                provideUserDataStep.performAction(ConfirmUserDataAction())  // compilation error: PASSED
//                provideCardDataStep.performAction(ConfirmCardDataAction()) // compilation error: PASSED
//                scarQRCode.performAction(ScanQRCodeAction()) // compilation error: PASSED
                notRelatedCancellableStep.performAction(NotRelatedAction())
            }
        }
    }

    // This method should never be run as a part of test suite - its purpose is to check the generics definition statically
    @Suppress("UNUSED_VARIABLE")
    fun genericsTest() = runBlocking {
        val provideUserDataStep = Step(ProvideUserData, "", Unit)
        val provideCardDataStep = Step(ProvideCardData, 5, Unit)
        val introductionToQRCodeStep = Step(IntroductionToQRCode, true, Unit)
        val scarQRCode = Step(ScanQRCode)
        val notRelatedCancellableStep = Step(NotRelatedCancellableStepType)

        val cancelPaymentAction = CancelPaymentAction()
        provideUserDataStep.performAction(cancelPaymentAction)
//        provideCardDataStep.performAction(cancelPaymentAction) // compilation error: PASSED
//        introductionToQRCodeStep.performAction(cancelPaymentAction) // compilation error: PASSED
        scarQRCode.performAction(cancelPaymentAction)
//        notRelatedCancellableStep.performAction(cancelPaymentAction) // compilation error: PASSED

        val confirmUserDataAction = ConfirmUserDataAction()
        provideUserDataStep.performAction(confirmUserDataAction)
//        provideCardDataStep.performAction(confirmUserDataAction) // compilation error: PASSED
//        introductionToQRCodeStep.performAction(confirmUserDataAction) // compilation error: PASSED
//        scarQRCode.performAction(confirmUserDataAction) // compilation error: PASSED
//        notRelatedCancellableStep.performAction(confirmUserDataAction) // compilation error: PASSED

        val confirmCardDataAction = ConfirmCardDataAction()
//        provideUserDataStep.performAction(confirmCardDataAction) // compilation error: PASSED
        provideCardDataStep.performAction(confirmCardDataAction)
//        introductionToQRCodeStep.performAction(confirmCardDataAction) // compilation error: PASSED
//        scarQRCode.performAction(confirmCardDataAction) // compilation error: PASSED
//        notRelatedCancellableStep.performAction(confirmCardDataAction) // compilation error: PASSED

        val readQRCodeFullManualAction = ReadQRCodeFullManualAction()
//        provideUserDataStep.performAction(readQRCodeFullManualAction) // compilation error: PASSED
//        provideCardDataStep.performAction(readQRCodeFullManualAction) // compilation error: PASSED
        introductionToQRCodeStep.performAction(readQRCodeFullManualAction)
        scarQRCode.performAction(readQRCodeFullManualAction)
//        notRelatedCancellableStep.performAction(readQRCodeFullManualAction) // compilation error: PASSED

        val confirmQRCodeIntroductionAction = ConfirmQRCodeIntroductionAction()
//        provideUserDataStep.performAction(confirmQRCodeIntroductionAction) // compilation error: PASSED
//        provideCardDataStep.performAction(confirmQRCodeIntroductionAction) // compilation error: PASSED
        introductionToQRCodeStep.performAction(confirmQRCodeIntroductionAction)
//        scarQRCode.performAction(confirmQRCodeIntroductionAction) // compilation error: PASSED
//        notRelatedCancellableStep.performAction(confirmQRCodeIntroductionAction) // compilation error: PASSED

        val scanQRCodeAction = ScanQRCodeAction()
//        provideUserDataStep.performAction(scanQRCodeAction) // compilation error: PASSED
//        provideCardDataStep.performAction(scanQRCodeAction) // compilation error: PASSED
//        introductionToQRCodeStep.performAction(scanQRCodeAction) // compilation error: PASSED
        scarQRCode.performAction(scanQRCodeAction)
//        notRelatedCancellableStep.performAction(scanQRCodeAction) // compilation error: PASSED
    }
}
