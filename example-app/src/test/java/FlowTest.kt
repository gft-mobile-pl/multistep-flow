import OnboardingStep.CollectUserName
import OnboardingStep.CollectUserNameTwo
import OnboardingStep.ComplexStep
import OnboardingStep.ComplexStep.ComplexStepUserInput
import OnboardingStep.ComplexStep.ComplexStepValidationResult
import OnboardingStep.ComplexStep.ComplexStepValidator
import OnboardingStep.ConfirmTermsAndConditions
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.StartMultiStepFlowUseCase
import com.gft.multistepflow.usecases.UpdateUserInputUseCase
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.CompositeUserInputValidator
import com.gft.multistepflow.validators.CompositeUserInputValidator.PartialValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import com.gft.multistepflow.validators.UserInputValidator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.random.Random

class FlowTest {

    @Test
    fun test2() = runBlocking {
        val step = Step(CollectUserName, "payload", "empty username", 5)

        val testFlow = OnboardingFlow()
        val startFlow = StartMultiStepFlowUseCase(testFlow)
        startFlow(step)

        val updateUseCase = UpdateUserInputUseCase(testFlow)

        updateUseCase.invoke(CollectUserName) {
            "first username"
        }
        println("#Test (first username set) $testFlow")

        updateUseCase.invoke(CollectUserName, CollectUserNameTwo) {
            "second username"
        }
        println("#Test (second username set) $testFlow")

    }

    @Test
    fun test3() = runBlocking {
        val step = Step(ConfirmTermsAndConditions, "payload", 0)

        val testFlow = OnboardingFlow()
        val startFlow = StartOnboardingFlowUseCase(testFlow)
        startFlow(step)

        val updateUseCase = UpdateUserInputUseCase(testFlow)
        updateUseCase.invoke(ConfirmTermsAndConditions) {
            it + 1
        }
        println("#Test (first consent set) $testFlow")

        updateUseCase.invoke(ConfirmTermsAndConditions) {
            it + 1
        }
        println("#Test (second consent set) $testFlow")

    }

    @Test
    fun test4() = runBlocking {
        val step = Step(
            type = ComplexStep,
            payload = Unit,
            userInput = ComplexStepUserInput("", null),
            validationResult = ComplexStepValidationResult(false, null),
            validator = ComplexStepValidator()
        )

        val testFlow = OnboardingFlow()
        val startFlow = StartMultiStepFlowUseCase(testFlow)
        startFlow(step)

        val updateUseCase = UpdateUserInputUseCase(testFlow)
        updateUseCase.invoke(ComplexStep) {
            it.copy(nickname = "aa")
        }
        println("#Test (first nickname set) $testFlow")

        updateUseCase.invoke(ComplexStep) {
            it.copy(nickname = "MySecondNickName")
        }
        println("#Test (second nickname set) $testFlow")


        updateUseCase.invoke(ComplexStep) {
            it.copy(age = 20)
        }
        println("#Test (first age set) $testFlow")

        updateUseCase.invoke(ComplexStep) {
            it.copy(age = null)
        }
        println("#Test (second age set) $testFlow")

        updateUseCase.invoke(ComplexStep) {
            it.copy(age = 3)
        }
        println("#Test (third age set) $testFlow")

    }
}

class PasswordValidator : UserInputValidator<String, Long?> {
    override fun validate(currentUserInput: String, newUserInput: String, currentValidationResult: Long?): Long? {
        println("#Test Validating $currentUserInput / $newUserInput / $currentValidationResult")
        return if (Random.nextBoolean()) Random.nextLong() else null
    }
}

object UnrelatedStepType : StepType<String, String, Int, DefaultNoOpValidator>

sealed interface OnboardingStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
    StepType<Payload, UserInput, ValidationResult, Validator> {
    object CollectUserName : OnboardingStep<String, String, Int, DefaultNoOpValidator>
    object CollectUserNameTwo : OnboardingStep<String, String, Int, UserInputValidator<String, Int>>
    object CollectPassword : OnboardingStep<Int, String, Long?, PasswordValidator>
    object CollectPasswordTwo : OnboardingStep<Boolean, String, Long?, DefaultNoOpValidator>
    object ConfirmTermsAndConditions : OnboardingStep<String, Int, Unit, DefaultNoOpValidator>

    object ComplexStep : OnboardingStep<Unit, ComplexStepUserInput, ComplexStepValidationResult, ComplexStepValidator> {
        data class ComplexStepUserInput(
            val nickname: String,
            val age: Int?
        )

        data class ComplexStepValidationResult(
            val nicknameFormatIsValid: Boolean,
            val ageIsValid: AgeValidationResult?
        )

        enum class AgeValidationResult {
            TOO_YOUNG,
            OK
        }

        class NickNameValidator : PartialValidator<String, Boolean> {
            override fun validate(value: String): Boolean {
                return value.length > 3
            }
        }

        class AgeValidator : PartialValidator<Int?, AgeValidationResult?> {
            override fun validate(value: Int?): AgeValidationResult? {
                return if (value != null) {
                    if (value > 18) AgeValidationResult.OK else AgeValidationResult.TOO_YOUNG
                } else null
            }
        }

        class ComplexStepValidator : CompositeUserInputValidator<ComplexStepUserInput, ComplexStepValidationResult>() {
            init {
                addValidator(
                    validator = NickNameValidator(),
                    valueProvider = { userInput.nickname },
                    resultSetter = { validationResult.copy(nicknameFormatIsValid = partialValidationResult) }
                )

                addValidator(
                    validator = AgeValidator(),
                    valueProvider = { userInput.age },
                    resultSetter = { validationResult.copy(ageIsValid = partialValidationResult) }
                )
            }
        }
    }
}

class OnboardingFlow : MultiStepFlow<OnboardingStep<*, *, *, *>>(true)

class StartOnboardingFlowUseCase(flow: OnboardingFlow) : StartMultiStepFlowUseCase<OnboardingStep<*, *, *, *>>(flow)
