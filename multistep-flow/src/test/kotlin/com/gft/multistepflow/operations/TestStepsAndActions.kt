package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiFlowAction
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.StepType
import com.gft.multistepflow.operations.PaymentStep.PaymentWithCardStep
import com.gft.multistepflow.operations.PaymentStep.PaymentWithQRCodeStep

/**
 * Steps
 */
interface CancellableStep
interface QRCodeFullManualProvider

sealed interface PaymentStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator> {
    sealed interface PaymentWithCardStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        PaymentStep<Payload, UserInput, ValidationResult, Validator>

    sealed interface PaymentWithQRCodeStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        PaymentStep<Payload, UserInput, ValidationResult, Validator>
}

data object NotRelatedCancellableStepType : StepType<Unit, Unit, Unit, DefaultNoOpValidator>, CancellableStep

// supports both Card payment and QRCode payment
data object ProvideUserData : PaymentWithCardStep<String, Unit, Unit, DefaultNoOpValidator>,
    PaymentWithQRCodeStep<String, Unit, Unit, DefaultNoOpValidator>, CancellableStep

// support Card payment only
data object ProvideCardData : PaymentWithCardStep<Int, Unit, Unit, DefaultNoOpValidator>

// support QR code payment only
data object IntroductionToQRCode : PaymentWithQRCodeStep<Boolean, Unit, Unit, DefaultNoOpValidator>, QRCodeFullManualProvider
data object ScanQRCode : PaymentWithQRCodeStep<Unit, Unit, Unit, DefaultNoOpValidator>, CancellableStep,
    QRCodeFullManualProvider

/**
 * Flows
 */
class PaymentWithCardFlow : MultiStepFlow<PaymentWithCardStep<*, *, *, *>>(historyEnabled = false)
class PaymentWithQRCodeFlow : MultiStepFlow<PaymentWithQRCodeStep<*, *, *, *>>(historyEnabled = false)

/**
 * Sugar
 * The type aliases and base classes below are optional -> one can use them to shorten Action definition
 */
abstract class AnyPaymentTypeAction<T> : MultiFlowAction<T, PaymentStep<*, *, *, *>>()
typealias PaymentWithCardAction<T> = Action<T, PaymentWithCardFlow>
typealias PaymentWithQRCodeAction<T> = Action<T, PaymentWithQRCodeFlow>

/**
 * Actions
 */
class CancelPaymentAction : AnyPaymentTypeAction<CancellableStep>() {
    override suspend fun perform(flow: MultiStepFlow<out PaymentStep<*, *, *, *>>, transactionId: String) {
//        flow.setStep(Step(ScanQRCode)) // compilation error: PASSED
//        flow.setStep(Step(ProvideCardData, 5, Unit)) // compilation error: PASSED
//        flow.setStep(Step(NotRelatedCancellableStepType)) // compilation error: PASSED

//        if (flow is PaymentWithCardFlow) flow.setStep(Step(ProvideCardData, 5, Unit))
    }
}

class ConfirmUserDataAction : AnyPaymentTypeAction<ProvideUserData>() {
    override suspend fun perform(flow: MultiStepFlow<out PaymentStep<*, *, *, *>>, transactionId: String) {
//        flow.setStep(Step(ScanQRCode)) // compilation error: PASSED
//        flow.setStep(Step(ProvideCardData, 5, Unit)) // compilation error: PASSED
//        flow.setStep(Step(NotRelatedCancellableStepType)) // compilation error: PASSED

//        if (flow is PaymentWithCardFlow) flow.setStep(Step(ProvideCardData, 5, Unit))
    }
}

class ConfirmCardDataAction : PaymentWithCardAction<ProvideCardData>() {
    override suspend fun perform(flow: PaymentWithCardFlow, transactionId: String) =
        println("#Test ConfirmCardDataAction.perform")
}

class ReadQRCodeFullManualAction : PaymentWithQRCodeAction<QRCodeFullManualProvider>() {
    override suspend fun perform(flow: PaymentWithQRCodeFlow, transactionId: String) {
        println("#Test ReadQRCodeFullManualAction.perform")

//        flow.setStep(Step(ScanQRCode))
//        flow.setStep(Step(ProvideCardData, 5, Unit)) // compilation error: PASSED
//        flow.setStep(Step(NotRelatedCancellableStepType)) // compilation error: PASSED
    }

}

class ConfirmQRCodeIntroductionAction : PaymentWithQRCodeAction<IntroductionToQRCode>() {
    override suspend fun perform(flow: PaymentWithQRCodeFlow, transactionId: String) =
        println("#Test ConfirmQRCodeIntroductionAction.perform")
}

class ScanQRCodeAction : PaymentWithQRCodeAction<ScanQRCode>() {
    override suspend fun perform(flow: PaymentWithQRCodeFlow, transactionId: String) =
        println("#Test ScanQRCodeAction.perform")
}
