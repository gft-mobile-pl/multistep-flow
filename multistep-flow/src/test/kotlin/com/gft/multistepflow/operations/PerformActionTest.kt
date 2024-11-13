package com.gft.multistepflow.operations

import com.gft.multistepflow.Step
import kotlinx.coroutines.runBlocking


class PerformActionTest {

    // This method should never be run as a part of test suite - its purpose is to check the generics definition statically
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
