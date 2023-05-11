# Multistep Flow

[[_TOC_]]

The Multistep Flow library enables the description of complex processes by dividing them into steps 
and provides a set of mechanisms for managing traversal between them. 
Flowy also supports gathering information provided by the user and managing errors that may occur 
during the process. With the help of Multistep Flow, both local processes and remate processes can be modeled.

# Usage

## Defining steps

All steps are instances of the `Step` class and are created by either domain-level components 
such as `Actions` and repositories, or by data-level components (e.g. services) in remotely controlled processes.
Each step has a type, which is defined by implementing the `StepType` interface.

```kotlin
interface StepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>>
```

While implementing the `StepType` interface one need to define a few generic types:

- Payload - contains data required by a view and actions. 
This data can either be defined locally or retrieved from a remote service, such as Zoral. 
It is important to note that if certain data can be obtained through alternative means, it is advisable not to include it in the payload.
While payloads are typically static, they can occasionally be modified by actions (e.g. Zoral may return same step with a modified payload).
- UserInput - contains data provided by a user or that can edited by a user. UserInput may be prepopulated with a data that comes 
from a remote service, previous steps etc. Use input may be updated with a `UpdateUserInputUseCase` or its descendants.
- ValidationResult - contains data describing the result of the validation of the user input. If a validator is defined
for the step, the validation occurs automatically each time a user input is updated with `UpdateUserInputUseCase`. 
Validation result may also come from a remote service in case of a remotely controlled processes.
- Validator - a component implementing `UserInputValidator` interface responsible for performing user input validation.
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
object CollectPassword : StepType<Unit, Password, PasswordValidationResult, MinLengthValidator>
```
In this example two step types are defined. They do not provide `Payload` (`Unit` is used), but allow the user to provide and edit `Username` and `Password`.
`CollectPassword` defines that `MinLengthValidator` will be applied to the user input and the result of validation is of `PasswordValidationResult` type.

> Tip: in the example above the `object` is used while defining step types. 
> Although one could use `interface` instead, the API of this library almost always expects **instances** of the step types. 
> <br />Just use `objects`. You have been warned ;)  

> Primitive types may be used instead of `value class` for `Payload`, `UserInput` and `ValidationResult`. 
> However, the use of more meaningful types is always suggested.

### Grouping steps

More than often it is useful to group all the steps that constitute a particular process/flow by utilizing the `sealed interface`:
```kotlin
sealed interface LoginStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator> {
    
    object CollectUsername : LoginStep<Unit, Username, Unit, DefaultNoOpValidator>
    object CollectPassword : LoginStep<Unit, Password, Int, MinLengthValidator>
}
```

Note: In Kotlin you may define "children" of the `sealed interface` anywhere within the same package. 
It means that you may achieve the same result with the following code:
```kotlin
// base step type (interface)
sealed interface LoginStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator>

// concrete step types (objects)
object CollectUsername : LoginStep<Unit, Username, Unit, DefaultNoOpValidator>
object CollectPassword : LoginStep<Unit, Password, Int, MinLengthValidator>
```
Both code snippets look very similar, but the second approach allows the step types to be defined in a different file than base step type as long it is placed in the same package.
This may be useful if you want to reuse the same step in a few related processes (you may find more on this below).

**Notes on generics**

Unfortunately, due to the way generics work, you must always include this static part in the base step interface definition:
```kotlin
<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> : StepType<Payload, UserInput, ValidationResult, Validator>
```
Simply copy/paste it as needed without any modifications.

#### Using same step in multiple processes

Sometimes one may want to use the same step within a few related processes.
```kotlin
@JvmInline
value class Passcode(val value: String)

// 1. Define the base step type for each process
sealed interface DefinePasscodeStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator>

sealed interface EditPasscodeStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator>

// 2. Define step and assign it to all related processes
object CollectNewPasscode : DefinePasscodeStep<Unit, Passcode, Unit, DefaultNoOpValidator>, EditPasscodeStep<Unit, Passcode, Unit, DefaultNoOpValidator>
```

## Creating flow

## Observing flow state

## Updating user input

## Defining validators

## Creating steps

## Defining actions

## Navigating to other steps

### Steps history
