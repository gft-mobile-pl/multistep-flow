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
One may use `Unit` for unused `Payload`, `UserInput` and `ValidationResult` and `DefaultNoOpValidator` if `Validator` is not required.

### Grouping steps

## Observing flow state

## Updating user input

## Defining validators

## Creating steps

## Defining actions

## Navigating to other steps

### Steps history
