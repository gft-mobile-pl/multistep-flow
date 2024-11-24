package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RestartFlowTest {
    sealed interface TestFlowStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private data object StepOne : TestFlowStep
    private data object StepTwo : TestFlowStep
    private data object StepThree : TestFlowStep
    private class TestFlow(historyEnabled: Boolean) : MultiStepFlow<TestFlowStep>(historyEnabled)

    private object NotRelatedTestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class NotRelatedTestFlow : MultiStepFlow<NotRelatedTestStep>(historyEnabled = false)

    @Test
    fun genericsTest() {
        object : Action<Any, TestFlowStep>() {
            override suspend fun perform(flow: MultiStepFlow<TestFlowStep>, transactionId: String) {
                val step = Step(StepOne)
                flow.restartFlow(step)

                @Suppress("UNUSED_VARIABLE") val unrelatedStep = Step(NotRelatedTestStep)
                @Suppress("UNUSED_VARIABLE") val unrelatedFlow = NotRelatedTestFlow()
//                unrelatedFlow.restartFlow(unrelatedStep) // compilation error: PASSED
//                flow.setStep(unrelatedStep) // compilation error: PASSED
            }
        }
    }

    @Test
    fun `given history enabled, when MultiStepFlow_restart(Step) is invoked, steps history is cleared and initial step is set`(): Unit =
        runBlocking {
            val stepOne = Step(StepOne)
            val stepTwo = Step(StepTwo)
            val stepThree = Step(StepThree)
            val testFlow = TestFlow(historyEnabled = true)

            testFlow.start(stepOne)
            stepOne.performAction(object : Action<Any, TestFlowStep>() {
                override suspend fun perform(flow: MultiStepFlow<TestFlowStep>, transactionId: String) {
                    flow.setStep(stepTwo)
                    flow.restartFlow(stepThree)
                }
            })

            assertEquals(stepThree, testFlow.session.data.value?.currentStep)
            assertEquals(stepThree, testFlow.session.data.value?.stepsHistory?.first())
            assertEquals(1, testFlow.session.data.value?.stepsHistory?.size)
        }

    @Test
    fun `given history disabled, when MultiStepFlow_restart(Step) is invoked, history is left empty and step is set`(): Unit =
        runBlocking {
            val stepOne = Step(StepOne)
            val stepTwo = Step(StepTwo)
            val stepThree = Step(StepThree)
            val testFlow = TestFlow(historyEnabled = false)

            testFlow.start(stepOne)
            stepOne.performAction(object : Action<Any, TestFlowStep>() {
                override suspend fun perform(flow: MultiStepFlow<TestFlowStep>, transactionId: String) {
                    flow.setStep(stepTwo)
                    flow.restartFlow(stepThree)
                }
            })

            assertEquals(stepThree, testFlow.session.data.value?.currentStep)
            assertEquals(0, testFlow.session.data.value?.stepsHistory?.size)
        }

    @Test(expected = IllegalFlowException::class)
    fun `when MultiStepFlow_restart(Step) is invoked on an unrelated step, IllegalFlowException is thrown`(): Unit =
        runBlocking {
            val step = Step(StepOne)
            val testFlow = TestFlow(historyEnabled = true)
            val unrelatedTestFlow = TestFlow(historyEnabled = true)

            testFlow.start(step)
            step.performAction(object : Action<Any, TestFlowStep>() {
                override suspend fun perform(flow: MultiStepFlow<TestFlowStep>, transactionId: String) {
                    unrelatedTestFlow.restartFlow(step)
                }
            })
        }
}