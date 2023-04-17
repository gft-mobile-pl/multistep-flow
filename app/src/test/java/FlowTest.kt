import OnboardingStep.CollectPassword
import OnboardingStep.CollectUserName
import com.gft.multistepflow.model.BaseUserInputValidator
import com.gft.multistepflow.model.DefaultNoOpValidator
import com.gft.multistepflow.model.UserInputValidator
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.UpdateUserInputUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.random.Random

class AltFlowTest {

    @Test
    fun test() = runBlocking {
        val testFlow = AltTestFlow()

        val step = Step(CollectUserName::class, "Enter username", "pp", )
        testFlow.start(step)

        val updateUseCase = AltUpdateTestFlowUseCase(testFlow)
        updateUseCase.invoke<CollectUserName, String> {
            "real password"
        }
        updateUseCase.invoke(CollectUserName::class, CollectPassword::class) {
            "real password 22222"
        }


        println("#Test $testFlow")




        // Step(OnboardingStep.DummyStep::class)
        // Step(OnboardingStep.DummyStep2::class, "payload")
        // Step(OnboardingStep.DummyStep3::class, "userInput")
    }
}

class PasswordValidator : UserInputValidator<String, Long> {
    override fun validate(currentUserInput: String, newUserInput: String, currentValidationResult: Long?): Long {
        println("#Test Validating $currentUserInput / $newUserInput / $currentValidationResult")
        return Random.nextLong()
    }
}

sealed interface OnboardingStep {
    interface CollectUserName : StepType<String, String, Int, DefaultNoOpValidator>, OnboardingStep
    interface CollectPassword : StepType<Int, String, Long, PasswordValidator>, OnboardingStep

    interface DummyStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>, OnboardingStep
    interface DummyStep2 : StepType<String, Unit, Unit, DefaultNoOpValidator>, OnboardingStep
    interface DummyStep3 : StepType<Unit, String, Unit, DefaultNoOpValidator>, OnboardingStep
}

class AltTestFlow : MultiStepFlow()

class AltUpdateTestFlowUseCase(flow: AltTestFlow) : UpdateUserInputUseCase(flow)


fun Step.Actions<CollectUserName>.getAction() {

}