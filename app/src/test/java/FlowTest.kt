import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.UserInputValidator
import com.gft.multistepflow.usecases.UpdateUserInputUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FlowTest {

    @Test
    fun test() = runBlocking {
        val testFlow = TestFlow()

        testFlow.start(MyStep())

        // val usecase = UpdateTestFlowUseCase(testFlow)
        // usecase.invoke<MyStep, String> { input ->
        //
        //     "aaaaaaaaaaaaaa"
        // }

    }
}

class MyStep : Step<MyStep, String, String, Int>(
    "PY",
    "UI",
    0,
    object : UserInputValidator<String, Int> {
        override fun <T : Int> validate(oldUserInput: String, newUserInput: String, oldValidationResult: Int?): T? {
            println("#Test MyStep validation: $oldUserInput, $newUserInput, $oldValidationResult")
            return 5 as T
        }
    }
) {
    override fun copy(payload: String, userInput: String, validationResult: Int?): MyStep {
        return MyStep()
    }
}

class TestFlow : MultiStepFlow()

class UpdateTestFlowUseCase(flow: TestFlow) : UpdateUserInputUseCase(flow)