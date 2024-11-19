package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.end
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class EndFlowTest {
    private object TestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class TestFlow : MultiStepFlow<TestStep>(historyEnabled = false)

    private object NotRelatedTestStep : StepType<Unit, Unit, Unit, DefaultNoOpValidator>
    private class NotRelatedTestFlow : MultiStepFlow<NotRelatedTestStep>(historyEnabled = false)

    private class TestFlowAction(
        private val block: suspend (flow: MultiStepFlow<TestStep>) -> Unit,
    ) : Action<TestStep, TestStep>() {
        override suspend fun perform(flow: MultiStepFlow<TestStep>, transactionId: String) {
            block(flow)
        }
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `when MultiStepFlow_end is invoked, await all actions before ending`(): Unit = runBlocking {
        val testStep = Step(TestStep)
        val testFlow = TestFlow()
        var hasLastActionEnded: Boolean = false
        val onLastActionHasBeenOrdered = Channel<Unit>()

        testFlow.start(testStep)
        async(start = CoroutineStart.UNDISPATCHED) {
            testStep.performAction(TestFlowAction {
                onLastActionHasBeenOrdered.receive()
            })
        }
        async(start = CoroutineStart.UNDISPATCHED) {
            testStep.performAction(TestFlowAction {
                hasLastActionEnded = true
            })
        }
        onLastActionHasBeenOrdered.send(Unit)

        testFlow.end()

        assertTrue(hasLastActionEnded)
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `when MultiStepFlow_end is invoked in action, await all pending actions before proceeding`(): Unit = runBlocking {
        val testStep = Step(TestStep)
        val testFlow = TestFlow()
        var hasLastActionEnded: Boolean = false
        val onFirstActionHasEnded = Channel<Unit>()
        val onLastActionHasBeenOrdered = Channel<Unit>()

        testFlow.start(testStep)

        async(start = CoroutineStart.UNDISPATCHED) {
            testStep.performAction(TestFlowAction { flow ->
                onLastActionHasBeenOrdered.receive()
                flow.end()
                onFirstActionHasEnded.send(Unit)
            })
        }
        async(start = CoroutineStart.UNDISPATCHED) {
            testStep.performAction(TestFlowAction {
                hasLastActionEnded = true
            })
        }
        onLastActionHasBeenOrdered.send(Unit)

        onFirstActionHasEnded.receive()
        assertTrue(hasLastActionEnded)
    }
}