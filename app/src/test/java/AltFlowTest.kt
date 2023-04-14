import OnboardingStep.CollectPassword
import OnboardingStep.CollectUserName
import com.gft.multistepflow.model.NoOpValidator
import com.gft.multistepflow.model.UserInputValidator
import com.gft.multistepflow.model.AltMultiStepFlow
import com.gft.multistepflow.model.AltStep
import com.gft.multistepflow.model.AltStepType
import com.gft.multistepflow.model.provideValidator
import com.gft.multistepflow.usecases.AltUpdateUserInputUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.random.Random

class AltFlowTest {

    @Test
    fun test() = runBlocking {
        val testFlow = AltTestFlow()

        val step = AltStep(CollectUserName::class, "Enter username", "pp")
        testFlow.start(step)

        val updateUseCase = AltUpdateTestFlowUseCase(testFlow)
        updateUseCase.invoke<CollectUserName, String> {
            "real password"
        }
        updateUseCase.invoke(CollectUserName::class, CollectPassword::class) {
            "real password 22222"
        }


        println("#Test $testFlow")




        AltStep(OnboardingStep.DummyStep::class)
        AltStep(OnboardingStep.DummyStep2::class, "payload")
        AltStep(OnboardingStep.DummyStep3::class, "userInput")
    }
}

class PasswordValidator : UserInputValidator<String, Long> {
    override fun <T : Long> validate(oldUserInput: String, newUserInput: String, oldValidationResult: Long?): T {
        println("#Test Validating $oldUserInput / $newUserInput / $oldValidationResult")
        return Random.nextLong() as T
    }
}

sealed interface OnboardingStep {
    interface CollectUserName : AltStepType<String, String, Int, NoOpValidator>, OnboardingStep
    interface CollectPassword : AltStepType<Int, String, Long, PasswordValidator>, OnboardingStep

    interface DummyStep : AltStepType<Unit, Unit, Unit, NoOpValidator>, OnboardingStep
    interface DummyStep2 : AltStepType<String, Unit, Unit, NoOpValidator>, OnboardingStep
    interface DummyStep3 : AltStepType<Unit, String, Unit, NoOpValidator>, OnboardingStep
}

class AltTestFlow : AltMultiStepFlow()

class AltUpdateTestFlowUseCase(flow: AltTestFlow) : AltUpdateUserInputUseCase(flow)


fun AltStep.Actions<CollectUserName>.getAction() {

}