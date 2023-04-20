import OnboardingStep.CollectPassword
import OnboardingStep.CollectPasswordTwo
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
    fun test1() = runBlocking {
        val step = Step(CollectPassword::class, 10, "empty password", null, PasswordValidator())

        val testFlow = OnboardingFlow()
        testFlow.start(step)

        val updateUseCase = UpdateUserInputUseCase(testFlow)
        updateUseCase.invoke<CollectPassword, String> {
            "first password"
        }
        println("#Test (first pass set) $testFlow")

        updateUseCase.invoke(CollectPassword::class, CollectPasswordTwo::class) {
            "second password"
        }
        println("#Test (second pass set) $testFlow")

    }

    @Test
    fun test2() = runBlocking {
        val step = Step(CollectUserName::class, "payload", "empty username", 5)

        val testFlow = OnboardingFlow()
        testFlow.start(step)

        val updateUseCase = UpdateUserInputUseCase(testFlow)
        updateUseCase.invoke<CollectUserName, String> {
            "first username"
        }
        println("#Test (first username set) $testFlow")

        updateUseCase.invoke(CollectUserName::class, CollectUserNameTwo::class) {
            "second username"
        }
        println("#Test (second username set) $testFlow")

        updateUseCase.invoke(CollectUserName) {
            "third username"
        }
        println("#Test (third username set) $testFlow")

        updateUseCase.invoke(CollectUserName, CollectUserNameTwo) {
            "fourth username"
        }
        println("#Test (third fourth set) $testFlow")

    }

    @Test
    fun test3() = runBlocking {
        val step = Step(ConfirmTermsAndConditions::class, "payload", 0)

        val testFlow = OnboardingFlow()
        testFlow.start(step)

        val updateUseCase = UpdateUserInputUseCase(testFlow)
        updateUseCase.invoke<ConfirmTermsAndConditions, Int> {
            it + 1
        }
        println("#Test (first consent set) $testFlow")

        updateUseCase.invoke(ConfirmTermsAndConditions::class) {
            it + 1
        }
        println("#Test (second consent set) $testFlow")

    }

    @Test
    fun test4() = runBlocking {
        val step = Step(
            type = ComplexStep::class,
            payload = Unit,
            userInput = ComplexStepUserInput("", null),
            validationResult = ComplexStepValidationResult(false, null),
            validator = ComplexStepValidator())

        val testFlow = OnboardingFlow()
        testFlow.start(step)

        val updateUseCase = UpdateUserInputUseCase(testFlow)
        updateUseCase.invoke<ComplexStep, ComplexStepUserInput> {
            it.copy(nickname = "aa")
        }
        println("#Test (first nickname set) $testFlow")

        updateUseCase.invoke(ComplexStep::class) {
            it.copy(nickname = "MySecondNickName")
        }
        println("#Test (second nickname set) $testFlow")


        updateUseCase.invoke<ComplexStep, ComplexStepUserInput> {
            it.copy(age = 20)
        }
        println("#Test (first age set) $testFlow")

        updateUseCase.invoke(ComplexStep::class) {
            it.copy(age = null)
        }
        println("#Test (second age set) $testFlow")

        updateUseCase.invoke(ComplexStep::class) {
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

sealed interface OnboardingStep<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> : StepType<Payload, UserInput, ValidationResult, Validator> {
    object CollectUserName : OnboardingStep<String, String, Int, DefaultNoOpValidator>
    object CollectUserNameTwo : OnboardingStep<String, String, Int, UserInputValidator<String, Int>>
    interface CollectPassword : OnboardingStep<Int, String, Long?, PasswordValidator>
    interface CollectPasswordTwo : OnboardingStep<Boolean, String, Long?, DefaultNoOpValidator>
    interface ConfirmTermsAndConditions : OnboardingStep<String, Int, Unit, DefaultNoOpValidator>

    interface ComplexStep : OnboardingStep<Unit, ComplexStepUserInput, ComplexStepValidationResult, ComplexStepValidator> {
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

class OnboardingFlow : MultiStepFlow<OnboardingStep<*, *, *, *>>()
