package com.gft.multistepflow.operations

import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.operations.PaymentStep.PaymentWithCardStep
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

        val payWithCardFlow = MultiStepFlow<PaymentWithCardStep<*, *, *, *>>(historyEnabled = true)
        payWithCardFlow.start(provideUserDataStep, assertFlowIsNotStarted = false)
        payWithCardFlow.start(provideCardDataStep, assertFlowIsNotStarted = false)
//        payWithCardFlow.start(introductionToQRCodeStep, assertFlowIsNotStarted = false) // compilation error: PASSED
//        payWithCardFlow.start(scarQRCode, assertFlowIsNotStarted = false) // compilation error: PASSED

        val payWithQRCodeFlow = MultiStepFlow<PaymentWithQRCodeStep<*, *, *, *>>(historyEnabled = true)
        payWithQRCodeFlow.start(provideUserDataStep, assertFlowIsNotStarted = false)
//        payWithQRCodeFlow.start(provideCardDataStep, assertFlowIsNotStarted = false) // compilation error: PASSED
        payWithQRCodeFlow.start(introductionToQRCodeStep, assertFlowIsNotStarted = false)
        payWithQRCodeFlow.start(scarQRCode, assertFlowIsNotStarted = false)
    }
}