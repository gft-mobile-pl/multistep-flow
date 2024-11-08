package com.gft.multistepflow.operations

import com.gft.multistepflow.Step
import kotlinx.coroutines.runBlocking
import org.junit.Test


class PerformActionGenericsTest {

    @Test
    fun performActionGenericsTest() = runBlocking {
        val provideUserDataStep = Step(ProvideUserData, "", Unit)
        val provideCardDataStep = Step(ProvideCardData, 5, Unit)
        val introductionToPayPalStep = Step(IntroductionToPayPal, true, Unit)
        val loginToPayPalStep = Step(LoginToPaypal)

        val cancelPaymentAction = CancelPaymentAction()
        provideUserDataStep.performAction(cancelPaymentAction)
//        provideCardDataStep.performAction(cancelPaymentAction) // compilation error: PASSED
//        introductionToPayPalStep.performAction(cancelPaymentAction) // compilation error: PASSED
        loginToPayPalStep.performAction(cancelPaymentAction)

        val confirmUserDataAction = ConfirmUserDataAction()
        provideUserDataStep.performAction(confirmUserDataAction)
//        provideCardDataStep.performAction(confirmUserDataAction) // compilation error: PASSED
//        introductionToPayPalStep.performAction(confirmUserDataAction) // compilation error: PASSED
//        loginToPayPalStep.performAction(confirmUserDataAction) // compilation error: PASSED

        val confirmCardDataAction = ConfirmCardDataAction()
//        provideUserDataStep.performAction(confirmCardDataAction) // compilation error: PASSED
        provideCardDataStep.performAction(confirmCardDataAction)
//        introductionToPayPalStep.performAction(confirmCardDataAction) // compilation error: PASSED
//        loginToPayPalStep.performAction(confirmCardDataAction) // compilation error: PASSED

        val showPayPalPoliciesAction = ShowPayPalPoliciesAction()
//        provideUserDataStep.performAction(showPayPalPoliciesAction)
//        provideCardDataStep.performAction(showPayPalPoliciesAction)
        introductionToPayPalStep.performAction(showPayPalPoliciesAction) // compilation error: PASSED
        loginToPayPalStep.performAction(showPayPalPoliciesAction) // compilation error: PASSED

        val confirmPayPalIntroductionAction = ConfirmPayPalIntroductionAction()
//        provideUserDataStep.performAction(confirmPayPalIntroductionAction) // compilation error: PASSED
//        provideCardDataStep.performAction(confirmPayPalIntroductionAction) // compilation error: PASSED
        introductionToPayPalStep.performAction(confirmPayPalIntroductionAction)
//        loginToPayPalStep.performAction(confirmPayPalIntroductionAction) // compilation error: PASSED

        val loginWithCredentialsToPaypalAction = LoginWithCredentialsToPaypalAction()
//        provideUserDataStep.performAction(loginWithCredentialsToPaypalAction) // compilation error: PASSED
//        provideCardDataStep.performAction(loginWithCredentialsToPaypalAction) // compilation error: PASSED
//        introductionToPayPalStep.performAction(loginWithCredentialsToPaypalAction) // compilation error: PASSED
        loginToPayPalStep.performAction(loginWithCredentialsToPaypalAction)
    }
}
