package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.Action
import com.gft.multistepflow.model.ActionError
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.NotActionErrorException
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.validators.BaseUserInputValidator
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerformActionUseCaseTest {
    private sealed interface TestStepType<Payload, UserInput, ValidationResult, Validator : BaseUserInputValidator<UserInput, ValidationResult, ValidationResult>> :
        StepType<Payload, UserInput, ValidationResult, Validator> {
        object TestFirstStepType : TestStepType<Unit, Unit, Unit, DefaultNoOpValidator>
    }

    private lateinit var testFlow: MultiStepFlow<TestStepType<*, *, *, *>>
    private lateinit var startMultiStepFlow: StartMultiStepFlowUseCase<TestStepType<*, *, *, *>>
    private lateinit var performAction: PerformActionUseCase

    @Before
    fun setUp() {
        testFlow = MultiStepFlow(historyEnabled = true)
        startMultiStepFlow = StartMultiStepFlowUseCase(testFlow)
        performAction = PerformActionUseCase(testFlow)
    }

    @Test
    fun `when action throws action error then it is not thrown`() {
        runTest {
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            class SafeFailingAction : Action() {
                override suspend fun perform(transactionId: String) {
                    throw ActionError(
                        error = IllegalStateException(),
                        action = this,
                        retryAllowed = false,
                        transactionId = transactionId
                    )
                }
            }

            val action = SafeFailingAction()

            performAction(action)

            assert(
                testFlow.session.requireData().currentStep.error.let {
                    it is ActionError && it.error is IllegalStateException
                }
            )
        }
    }

    @Test(expected = NotActionErrorException::class)
    fun `when action throws error that is not action error then NotActionErrorException is thrown`() {
        runTest {
            startMultiStepFlow(Step(TestStepType.TestFirstStepType))
            class UnsafeFailingAction : Action() {
                override suspend fun perform(transactionId: String) {
                    throw IllegalStateException()
                }
            }

            val action = UnsafeFailingAction()

            performAction(action)

            // exception is thrown
        }
    }
}
