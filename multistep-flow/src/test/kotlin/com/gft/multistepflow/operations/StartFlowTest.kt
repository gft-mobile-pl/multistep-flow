package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.operations.PaymentStep.PaymentWithQRCodeStep
import com.gft.multistepflow.start
import kotlinx.coroutines.runBlocking
import org.junit.Test

class StartFlowTest {
    @Test
    fun genericsTest() = runBlocking{
        val provideUserDataStep = Step(ProvideUserData, "", Unit)
        val provideCardDataStep = Step(ProvideCardData, 5, Unit)
        val introductionToQRCodeStep = Step(IntroductionToQRCode, true, Unit)
        val scarQRCode = Step(ScanQRCode)

        val payWithCardFlow = PaymentWithCardFlow()
        payWithCardFlow.start(provideUserDataStep, assertFlowIsNotStarted = false)
        payWithCardFlow.start(provideCardDataStep, assertFlowIsNotStarted = false)
//        payWithCardFlow.start(introductionToQRCodeStep, assertFlowIsNotStarted = false) // compilation error: PASSED
//        payWithCardFlow.start(scarQRCode, assertFlowIsNotStarted = false) // compilation error: PASSED

        val payWithQRCodeFlow = PaymentWithQRCodeFlow()
        payWithQRCodeFlow.start(provideUserDataStep, assertFlowIsNotStarted = false)
//        payWithQRCodeFlow.start(provideCardDataStep, assertFlowIsNotStarted = false) // compilation error: PASSED
        payWithQRCodeFlow.start(introductionToQRCodeStep, assertFlowIsNotStarted = false)
        payWithQRCodeFlow.start(scarQRCode, assertFlowIsNotStarted = false)

        @Suppress("UNUSED_VARIABLE")
        val action = object : Action<Any, PaymentWithQRCodeStep<*, *, *, *>>() {
            override suspend fun perform(flow: MultiStepFlow<PaymentWithQRCodeStep<*, *, *, *>>, transactionId: String) {
//                flow.start(scarQRCode)  // compilation error: PASSED
                payWithCardFlow.start(provideUserDataStep) // no error: PASSED
            }
        }
    }
}