# Multistep Flow

[[_TOC_]]

The **Multistep Flow** library enables the description of complex processes by dividing them into steps 
and provides a set of mechanisms for managing traversal between them.
**Multistep Flow** also supports gathering information provided by the user and managing errors that may occur 
during the process. With the help of Multistep Flow, both local processes and remate processes can be modeled.

# Usage

## Defining flows

We use a term `Flow` for a process that is modelled with **Multistep Flow** library. Each flow is described by:
- multiple step definitions in form of `StepType-s`
- an instance of `MultiStepFlow` which is responsible for hosting the instances of `Step-s`.

Each flow is managed with a bunch of use-cases which will be described later.

### Defining steps

All steps are instances of the `Step` class and are created by either domain-level components 
such as `Actions` and repositories, or by data-level components (e.g. services) in case of remotely controlled processes.
Each step has a type, which is defined by implementing the `StepType` interface.

```kotlin
interface StepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>>
```

While implementing the `StepType` interface one need to define a few generic types:

- **Payload** - contains data required by a view and actions. 
This data can either be defined locally or retrieved from a remote service, such as Zoral. 
It is important to note that if certain data can be obtained through alternative means, it is advisable not to include it in the payload.
While payloads are typically static, they can occasionally be modified by actions (e.g. Zoral may return same step with a modified payload).
- **UserInput** - contains data provided by a user or that can edited by a user. UserInput may be prepopulated with a data that comes 
from a remote service, previous steps etc. Use input may be updated with a `UpdateUserInputUseCase` or its descendants.
- **ValidationResult** - contains data describing the result of the validation of the user input. If a validator is defined
for the step, the validation occurs automatically each time a user input is updated with `UpdateUserInputUseCase`. 
Validation result may also come from a remote service in case of a remotely controlled processes.
- **Validator** - a component implementing `UserInputValidator` interface responsible for performing user input validation.
Validation is performed each the user input is updated as a result of user action. Note that if the user input is changed by a remote service 
(that is same step is returned with a different user input) the `Validator` will not be applied to the user input automatically.

All generics types of `StepType` are optional. 
One may use `Unit` for unused `Payload`, `UserInput` or `ValidationResult` and `DefaultNoOpValidator` if `Validator` is not required.

**Example**
```kotlin
@JvmInline
value class Username(val value: String)

@JvmInline
value class Password(val value: String)

enum class PasswordValidationResult {
    VALID, TOO_SHORT
}

object CollectUsername : StepType<Unit, Username, Unit, DefaultNoOpValidator>
object CollectPassword : StepType<Unit, Password, PasswordValidationResult?, MinLengthValidator>
```
In this example two step types are defined. They do not provide `Payload` (`Unit` is used), but allow the user to provide and edit `Username` and `Password`.
`CollectPassword` defines that `MinLengthValidator` will be applied to the user input and the result of validation is of `PasswordValidationResult` type.

In order to create a step of a given type it is enough to instantiate a `Step` class:
```kotlin
val collectUsernameStep = Step(
    type = CollectUsername,
    payload = Unit,
    userInput = Username("")
)

val collectPasswordStep = Step(
    type = CollectPassword,
    payload = Unit,
    userInput = Password(""),
    validationResult = null,
    validator = MinLengthValidator()
)
```

> 💡 In the example above the `object` is used while defining step types. 
> Although one could use `interface` instead, the API of this library almost always expects **instances** of the step types. 
> <br />Just use `objects`. You have been warned ;)  

> ⚠ Primitive types may be used instead of `value class` for `Payload`, `UserInput` and `ValidationResult`. 
> However, the use of more meaningful types is always suggested.



### Open-flow

Open-flow is a set of steps which types are defined independently, e.g.

```kotlin
object CollectUsername: StepType<Unit, Username, Unit, DefaultNoOpValidator>
object CollectPassword: StepType<Unit, Password, PasswordFormatValidationResult, PasswordFormatValidator>
object CollectPhoneNumber : StepType<DisplayName, PhoneNumber, PhoneNumberValidationResult, PhoneNumberFormatValidator>
object CollectOtp : StepType<DisplayName, Otp, PasswordFormatValidationResult, DefaultNoOpValidator>
```
> ℹ Although these steps are logically connected, the relationship between them is not defined in the code.

As the steps are not related one need to provide a `StepType` for the generic parameter of `MultistepFlow`:
```kotlin
val loginFlow = MultistepFlow<StepType<*, *, *, *>>(historyEnabled = true)
val onboardingFlow = MultistepFlow<StepType<*, *, *, *>>(historyEnabled = true)
```

> ⚠ From the DI perspective the `loginFlow` and `onboardingFlow` are indistinguishable. There are two solutions to this problem:
> - `Qualifiers`
> - creating separate classes for each flow, e.g.
>  ```kotlin
>  class LoginFlow : MultistepFlow<StepType<*, *, *, *>>(historyEnabled = true)
>  class OnboardingFlow : MultistepFlow<StepType<*, *, *, *>>(historyEnabled = true)
>  ```

### Sealed-flow

Sealed-flow is a set of steps which types inherit from the same base sealed step type, e.g.
```kotlin
sealed interface LoginStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> : StepType<Payload, UserInput, ValidationResult, Validator> {
    object CollectUsername: LoginStep<Unit, Username, Unit, DefaultNoOpValidator>
    object CollectPassword: LoginStep<Unit, Password, PasswordFormatValidationResult, PasswordFormatValidator>
    object CollectPhoneNumber : LoginStep<DisplayName, PhoneNumber, PhoneNumberValidationResult, PhoneNumberFormatValidator>
    object CollectOtp : LoginStep<DisplayName, Otp, PasswordFormatValidationResult, DefaultNoOpValidator>
}
```

> 💡 The generics part of the `LoginStep` definition may look scary, but fortunately it is the same for all base step types and does not require editing.
> You may simply copy/paste the following part:
> ```
> <Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> : StepType<Payload, UserInput, ValidationResult, Validator>
> ```

As all the step types inherit from the same base type (e.g. `LoginStep`) one may create a `MultistepFlow` which can host only steps of a particular:
```kotlin
val loginFlow = MultistepFlow<LoginStep<*, *, *, *>>(historyEnabled = true)
val onboardingFlow = MultistepFlow<OnboardingStep<*, *, *, *>>(historyEnabled = true)
```
> ⚠ Unfortunately **Koin** does not support generics and won't be able to distinguish `MultistepFlow<LoginStep<*, *, *, *>>` from `MultistepFlow<OnboardingStep<*, *, *, *>>`.
> You need to address this problem with qualifiers or create separate classes for each flow - check similar comment to "Open-flow".
> This issue does not affect **Dagger** or **Hilt** 

There are several advantages of using sealed-flows:
- exhaustive `when` can be used while dealing with the step types,
- unrelated steps cannot be inserted into `MultistepFlow`,
- use-cases that manages the flow can be typed to the base type which prevents their usage with unrelated flow.

#### Using same step in multiple flows

In Kotlin you may define "children" of the `sealed interface` anywhere within the same package, even in a different file: 
```kotlin
// file1.kt
sealed interface LoginStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator>

// file2.kt
object CollectUsername: LoginStep<Unit, Username, Unit, DefaultNoOpValidator>
object CollectPassword: LoginStep<Unit, Password, PasswordFormatValidationResult, PasswordFormatValidator>
object CollectPhoneNumber : LoginStep<DisplayName, PhoneNumber, PhoneNumberValidationResult, PhoneNumberFormatValidator>
object CollectOtp : LoginStep<DisplayName, Otp, PasswordFormatValidationResult, DefaultNoOpValidator>
```

This language feature make it possible to use the same step within a few flows:
```kotlin
@JvmInline
value class Passcode(val value: String)

// 1. Define the base step type for each flow
sealed interface DefinePasscodeStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> : StepType<Payload, UserInput, ValidationResult, Validator>
sealed interface EditPasscodeStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> : StepType<Payload, UserInput, ValidationResult, Validator>

// 2. Define step and assign it to all related flow
object CollectNewPasscode : 
    DefinePasscodeStep<Unit, Passcode, Unit, DefaultNoOpValidator>, 
    EditPasscodeStep<Unit, Passcode, Unit, DefaultNoOpValidator>

// 3. Create flows
val definePasscodeFlow = MultistepFlow<DefinePasscodeStep<*, *, *, *>>(historyEnabled = true)
val editPasscodeFlow = MultistepFlow<EditPasscodeStep<*, *, *, *>>(historyEnabled = true)
```

Such optimization may be tempting, but most of the time it is better to define a few similar steps (one for each flow) for improved readability. 
Note that all the use-cases provided by **Multistep Flow** library support managing multiple step types at the same time.

## Observing flow state

## Updating user input

## Defining validators

## Creating steps

## Defining actions

## Navigating to other steps

### Steps history

# Testing

The multistep flow library provides a test fixture that allows you to test your actions easily. By design, the action's `perform` method is protected and shouldn't be called directly - it is invoked only by `PerformActionUseCase`. This makes testing of an action a bit tricky since you need to make it accessible first using reflection.

To circumvent that, you can write your actions as simple as possible - just to interoperate with the flow (getting data from payloads and user inputs, setting the steps) and move all your business logic into separate, testable use cases. However, if you want to test the more complex actions, you can use a provided test fixture.

Test fixture contains a set of methods:

| Method                                                                                                                                   | Purpose                                                                                                                                                                                                                                                    |
|------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `MultistepFlowTestFixture.restartTestFlow()`                                                                                             | This method should be called in `@BeforeEach` method. Its purpose is to clear the flow of any remaining errors and make sure that it's in the clean state for each test. Note that test flow is started by default and there is no need to start it first. |
| `MultistepFlowTestFixture.testAction(action: Action, dispatcher: CoroutineDispatcher, transactionId: String = UUID.randomUUID().toString())` 
 `MultistepFlowTestFixture.testAction(action: Action, transactionId: String = UUID.randomUUID().toString())`                                  | these two methods are mimicking the `PerformActionUseCase` interface and should be used to call the action for testing.                                                                                                                                    |
| `MultistepFlowTestFixture.getCurrentTestFlowError()`                                                                                                       | This method is used to retrieve any error thrown by performing an action. As all errors thrown by Action should be wrapped in `ActionError`, we expect here to get either `ActionError` or `null` if no error was thrown.                                   |
