package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.BaseUserInputValidator
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.operations.UpdateUserInputTest.TestStepType.FirstStep
import com.gft.multistepflow.operations.UpdateUserInputTest.TestStepType.FourthStep
import com.gft.multistepflow.operations.UpdateUserInputTest.TestStepType.SecondStep
import com.gft.multistepflow.operations.UpdateUserInputTest.TestStepType.ThirdStep
import com.gft.multistepflow.performAction
import com.gft.multistepflow.requireStep
import com.gft.multistepflow.start
import com.gft.multistepflow.updateUserInput
import com.gft.multistepflow.whenStep
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private const val UserInputDataOne = "one"
private const val UserInputDataTwo = "two"

class UpdateUserInputTest {
    private data class UserInput(val data: String)

    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        data object FirstStep : TestStepType<Unit, UserInput, Boolean, DefaultNoOpValidator>
        data object SecondStep : TestStepType<Unit, UserInput, Boolean, DefaultNoOpValidator>
        data object ThirdStep : TestStepType<Unit, Int, Unit, DefaultNoOpValidator>
        data object FourthStep : TestStepType<Unit, Int, Unit, DefaultNoOpValidator>
    }

    private class TestFlow(historyEnabled: Boolean = false) :
        MultiStepFlow<TestStepType<*, *, *, *>>(historyEnabled) {
        suspend fun performInActionScope(block: suspend Action<Any, TestStepType<*, *, *, *>>.() -> Unit) {
            @Suppress("UNCHECKED_CAST")
            ((session.data.value?.currentStep as? Step<TestStepType<*, *, *, *>, *, *, *, *>)?.performAction?.invoke(
                object : Action<Any, TestStepType<*, *, *, *>>() {
                    override suspend fun perform(
                        flow: MultiStepFlow<TestStepType<*, *, *, *>>,
                        transactionId: String
                    ) {
                        block()
                    }
                }))
        }
    }

    private object NotRelatedTestStep : StepType<Unit, String, Unit, DefaultNoOpValidator>
    private class NotRelatedTestFlow : MultiStepFlow<NotRelatedTestStep>(historyEnabled = false)

    @Test
    fun genericsTest(): Unit = runBlocking {
        val testFlow = TestFlow()
        val notRelatedTestFlow = NotRelatedTestFlow()

        testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))
        notRelatedTestFlow.start(Step(NotRelatedTestStep, Unit, "", Unit))

        testFlow.whenStep(FirstStep) {
            testFlow.updateUserInput { userInput ->
                userInput.copy(
                    data = UserInputDataTwo
                )
            }

            //notRelatedTestFlow.updateUserInput { userInput -> userInput } // compilation error: passed
            notRelatedTestFlow.updateUserInput(NotRelatedTestStep) { userInput -> userInput }
        }

        notRelatedTestFlow.whenStep(NotRelatedTestStep) {
            // testFlow.updateUserInput { userInput -> userInput } // compilation error: passed
            notRelatedTestFlow.updateUserInput { userInput -> userInput }
        }
    }

    @Test
    fun `given flow is started, when updateUserInput is called with matching step, update user input`(): Unit = runBlocking {
        val testFlow = TestFlow()
        testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))

        testFlow.updateUserInput(FirstStep) { userInput ->
            userInput.copy(
                data = UserInputDataTwo
            )
        }
        assertEquals(UserInputDataTwo, testFlow.requireStep(FirstStep).userInput.data)
    }

    @Test
    fun `given flow is started, when updateUserInput is called with matching steps, update user input`(): Unit = runBlocking {
        val testFlow = TestFlow()
        testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))

        testFlow.updateUserInput(FirstStep, SecondStep) { userInput ->
            userInput.copy(
                data = UserInputDataTwo
            )
        }
        assertEquals(UserInputDataTwo, testFlow.requireStep(FirstStep).userInput.data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given flow is started, when updateUserInput is called with not matching step, throw an exception`(): Unit = runBlocking {
        val testFlow = TestFlow()
        testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))

        testFlow.updateUserInput(ThirdStep, FourthStep) { userInput ->
            userInput
        }
    }

    @Test(expected = IllegalFlowStateException::class)
    fun `given flow is not, when updateUserInput is called, throw an error`(): Unit = runBlocking {
        val testFlow = TestFlow()
        testFlow.updateUserInput(FirstStep, SecondStep) { userInput ->
            userInput.copy(
                data = UserInputDataTwo
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given flow is started, when updateUserInput is called with not matching but compatible step, throw an exception`(): Unit = runBlocking {
        val testFlow = TestFlow()
        testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))

        testFlow.updateUserInput(SecondStep) { userInput ->
            userInput
        }
    }

    @Test
    fun `given in whenStepScope, when updateUserInput is called without any step type, update user input`(): Unit = runBlocking {
        val testFlow = TestFlow()

        testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))
        testFlow.whenStep(FirstStep) {
            testFlow.updateUserInput { userInput ->
                userInput.copy(
                    data = UserInputDataTwo
                )
            }
        }

        assertEquals("two", testFlow.requireStep(FirstStep).userInput.data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given in whenStepScope and step has changed in the meantime, when updateUserInput is called without any step type, then error is thrown`(): Unit = runBlocking {
        val testFlow = TestFlow()

        testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))
        testFlow.whenStep(FirstStep) {
            runBlocking {
                testFlow.performInActionScope {
                    testFlow.setStep(Step(ThirdStep, Unit, 5, Unit))
                }
            }
            testFlow.updateUserInput { userInput ->
                userInput.copy(
                    data = UserInputDataTwo
                )
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `given in whenStepScope and step has changed in the meantime to not matching but compatible step, when updateUserInput is called without any step type, then error is thrown`(): Unit =
        runBlocking {
            val testFlow = TestFlow()

            testFlow.start(Step(FirstStep, Unit, UserInput(UserInputDataOne), true))
            testFlow.whenStep(FirstStep) {
                runBlocking {
                    testFlow.performInActionScope {
                        testFlow.setStep(Step(SecondStep, Unit, UserInput(UserInputDataOne), true))
                    }
                }
                testFlow.updateUserInput { userInput -> userInput }
            }
        }

}