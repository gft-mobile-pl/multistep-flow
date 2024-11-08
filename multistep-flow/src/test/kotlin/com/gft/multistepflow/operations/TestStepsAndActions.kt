package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.StepType
import com.gft.multistepflow.operations.PaymentStep.PaymentWithCardStep
import com.gft.multistepflow.operations.PaymentStep.PaymentWithPayPalStep

/**
 * Steps
 */
interface CancellableStep
interface ShowPayPalPolicies

sealed interface PaymentStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator> {
    sealed interface PaymentWithCardStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        PaymentStep<Payload, UserInput, ValidationResult, Validator>

    sealed interface PaymentWithPayPalStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        PaymentStep<Payload, UserInput, ValidationResult, Validator>
}

// supports both Card payment and PayPal payment
data object ProvideUserData : PaymentStep<String, Unit, Unit, DefaultNoOpValidator>, CancellableStep

// support Card payment only
data object ProvideCardData : PaymentWithCardStep<Int, Unit, Unit, DefaultNoOpValidator>

// support Paypal payment only
data object IntroductionToPayPal : PaymentWithPayPalStep<Boolean, Unit, Unit, DefaultNoOpValidator>, ShowPayPalPolicies
data object LoginToPaypal : PaymentWithPayPalStep<Unit, Unit, Unit, DefaultNoOpValidator>, CancellableStep, ShowPayPalPolicies

/**
 * Actions
 */

// these type aliases are optional -> we use them to shorten Action definition
typealias PaymentAction<T> = Action<T, PaymentStep<*, *, *, *>>
typealias PaymentWithCardAction<T> = Action<T, PaymentWithCardStep<*, *, *, *>>
typealias PaymentWithPayPalAction<T> = Action<T, PaymentWithPayPalStep<*, *, *, *>>

class CancelPaymentAction : PaymentAction<CancellableStep>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test CancelPaymentAction.perform")
}

class ConfirmUserDataAction : PaymentWithCardAction<ProvideUserData>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ConfirmUserDataAction.perform")
}

class ConfirmCardDataAction : PaymentWithPayPalAction<ProvideCardData>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ConfirmCardDataAction.perform")
}

class ShowPayPalPoliciesAction : PaymentWithPayPalAction<ShowPayPalPolicies>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ShowPayPalPoliciesAction.perform")
}

class ConfirmPayPalIntroductionAction : PaymentWithPayPalAction<IntroductionToPayPal>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test ConfirmPayPalIntroductionAction.perform")
}

class LoginWithCredentialsToPaypalAction : PaymentWithPayPalAction<LoginToPaypal>() {
    override suspend fun ActionScope.perform(transactionId: String) = println("#Test LoginWithCredentialsToPaypalAction.perform")
}
