package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.NotActionErrorException
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.end
import com.gft.multistepflow.start
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class EndFlowTest {
    private object TestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class TestFlow : MultiStepFlow<TestStep>(historyEnabled = false)
    private class LongAction : Action<TestStep, TestFlow>() {
        override suspend fun perform(flow: TestFlow, transactionId: String) {
            delay(500)
        }
    }

    private class EndFlowAction : Action<TestStep, TestFlow>() {
        override suspend fun perform(flow: TestFlow, transactionId: String) {
//            flow.end() // compilation error -> expected behaviour
            flow.endImmediately()
        }
    }

    private class FailingAction : Action<TestStep, TestFlow>() {
        override suspend fun perform(flow: TestFlow, transactionId: String) {
            throw RuntimeException("Exception during FailingAction.perform")
        }
    }

    private class EndNotRelatedTestFlowAction(
        private val notRelatedFlow: NotRelatedTestFlow,
    ) : Action<TestStep, TestFlow>() {
        override suspend fun perform(flow: TestFlow, transactionId: String) {
            notRelatedFlow.endImmediately()
        }

    }

    private object NotRelatedTestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class NotRelatedTestFlow : MultiStepFlow<NotRelatedTestStep>(historyEnabled = false)

    @Test
    fun `when MultiStepFlow_endImmediately is invoked, cancel all pending actions`(): Unit = runBlocking {
        val testStep = Step(TestStep)
        val testFlow = TestFlow()

        testFlow.start(testStep)
        awaitAll(
            async(start = CoroutineStart.LAZY) { testStep.performAction(LongAction()) },
            async(start = CoroutineStart.LAZY) { testStep.performAction(EndFlowAction()) },
            async(start = CoroutineStart.LAZY) { testStep.performAction(FailingAction()) }
        )
    }

    @Suppress("DeferredResultUnused")
    @Test(expected = NotActionErrorException::class)
    fun `when MultiStepFlow_end is invoked, perform all pending actions before ending flow`(): Unit = runBlocking {
        val testStep = Step(TestStep)
        val testFlow = TestFlow()

        testFlow.start(testStep)
        async(start = CoroutineStart.UNDISPATCHED) {
            testStep.performAction(LongAction())
        }
        async(start = CoroutineStart.UNDISPATCHED) {
            testStep.performAction(FailingAction())
        }
        testFlow.end()
    }

    @Test(expected = InvalidFlowException::class)
    fun `when MultiStepFlow_endImmediately is invoked on unrelated flow, throw InvalidFlowException`(): Unit = runBlocking {
        val testStep = Step(TestStep)
        val testFlow = TestFlow()
        val notRelatedTestStep = Step(NotRelatedTestStep)
        val notRelatedTestFlow = NotRelatedTestFlow()

        testFlow.start(testStep)
        notRelatedTestFlow.start(notRelatedTestStep)

        try {
            testStep.performAction(EndNotRelatedTestFlowAction(notRelatedTestFlow))
        } catch (error: Exception) {
            throw error.getRootCause()
        }

    }
}

private fun Exception.getRootCause() = if (this is NotActionErrorException) this.error else this