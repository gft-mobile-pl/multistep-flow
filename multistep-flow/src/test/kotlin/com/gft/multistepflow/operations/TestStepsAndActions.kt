package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
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

// supports both Card payment and QRCode payment
data object ProvideUserData : PaymentWithCardStep<String, Unit, Unit, DefaultNoOpValidator>, PaymentWithQRCodeStep<String, Unit, Unit, DefaultNoOpValidator>, CancellableStep

// support Card payment only
data object ProvideCardData : PaymentWithCardStep<Int, Unit, Unit, DefaultNoOpValidator>

// support QR code payment only
data object IntroductionToQRCode : PaymentWithQRCodeStep<Boolean, Unit, Unit, DefaultNoOpValidator>, QRCodeFullManualProvider
data object ScanQRCode : PaymentWithQRCodeStep<Unit, Unit, Unit, DefaultNoOpValidator>, CancellableStep, QRCodeFullManualProvider

/**
 * Actions
 */

// these type aliases are optional -> we use them to shorten Action definition
typealias PaymentAction<T> = Action<T, PaymentStep<*, *, *, *>>
typealias PaymentWithCardAction<T> = Action<T, PaymentWithCardStep<*, *, *, *>>
typealias PaymentWithQRCodeAction<T> = Action<T, PaymentWithQRCodeStep<*, *, *, *>>

class CancelPaymentAction : PaymentAction<CancellableStep>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test CancelPaymentAction.perform")
}

class ConfirmUserDataAction : PaymentWithCardAction<ProvideUserData>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ConfirmUserDataAction.perform")
}

class ConfirmCardDataAction : PaymentWithQRCodeAction<ProvideCardData>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ConfirmCardDataAction.perform")
}

class ReadQRCodeFullManualAction : PaymentWithQRCodeAction<QRCodeFullManualProvider>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ReadQRCodeFullManualAction.perform")
}

class ConfirmQRCodeIntroductionAction : PaymentWithQRCodeAction<IntroductionToQRCode>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ConfirmQRCodeIntroductionAction.perform")
}

class ScanQRCodeAction : PaymentWithQRCodeAction<ScanQRCode>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ScanQRCodeAction.perform")
}
