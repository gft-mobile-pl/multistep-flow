package com.gft.multistepflow.operations

import com.gft.multistepflow.Action
import com.gft.multistepflow.DefaultNoOpValidator
import com.gft.multistepflow.MultiStepFlow
import com.gft.multistepflow.MultiStepFlow.Lifecycle
import com.gft.multistepflow.Step
import com.gft.multistepflow.StepType
import com.gft.multistepflow.clear
import com.gft.multistepflow.performAction
import com.gft.multistepflow.start
import com.gft.multistepflow.utils.asyncUndispatchedOnUnconfinedDispatcher
import io.mockk.Runs
import io.mockk.called
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test


class ClearFlowTest {
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

    @Test
    fun `given no Action in progress, when MultiStepFlow_clear is invoked, clear the flow`(): Unit = runBlocking {
        val testStep = Step(TestStep)
        val testFlow = TestFlow()

        testFlow.start(testStep)
        testFlow.clear()

        assertNull(testFlow.session.data.value)
        assertEquals(Lifecycle.State.NotInitialized, testFlow.lifecycle.value)
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given an Action in progress, when MultiStepFlow_clear outside of the Action, cancel the action`(): Unit = runBlocking {
        val testStep = Step(TestStep)
        val testFlow = TestFlow()
        val onContinueAction = Channel<Unit>()
        val lastActionTask = mockk<Runnable> { every { run() } just Runs }
        var actionResult: Result<*>? = null

        testFlow.start(testStep)
        asyncUndispatchedOnUnconfinedDispatcher {
            actionResult = testStep.performAction(TestFlowAction {
                onContinueAction.receive()
                lastActionTask.run()
            })
        }

        testFlow.clear()
        onContinueAction.trySend(Unit)

        verify {
            lastActionTask wasNot called
        }

        assertTrue(actionResult?.exceptionOrNull() is ClearFlowException)
        assertNull(testFlow.session.data.value)
        assertEquals(Lifecycle.State.NotInitialized, testFlow.lifecycle.value)
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given an Action in progress, when MultiStepFlow_clear invoked inside the Action being performed, cancel the action`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow()
            val lastActionTask = mockk<Runnable> { every { run() } just Runs }
            var actionResult: Result<*>? = null

            testFlow.start(testStep)
            asyncUndispatchedOnUnconfinedDispatcher {
                actionResult = testStep.performAction(TestFlowAction {
                    testFlow.clear()
                    lastActionTask.run()
                })
            }

            verify {
                lastActionTask wasNot called
            }

            assertNull(actionResult?.exceptionOrNull())
            assertTrue(actionResult!!.isSuccess)
            assertNull(testFlow.session.data.value)
            assertEquals(Lifecycle.State.NotInitialized, testFlow.lifecycle.value)
        }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given a non-cancellable Action is in progress, when MultiStepFlow_clear invoked await Action termination before clearing the flow`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow()
            var actionResult: Result<*>? = null
            val onContinueAction = Channel<Unit>()

            testFlow.start(testStep)
            asyncUndispatchedOnUnconfinedDispatcher {
                actionResult = testStep.performAction(TestFlowAction {
                    withContext(NonCancellable) {
                        onContinueAction.receive()
                    }
                })
            }

            asyncUndispatchedOnUnconfinedDispatcher {
                testFlow.clear()
            }

            assertTrue(testFlow.lifecycle.value is Lifecycle.State.Clearing)

            onContinueAction.send(Unit)

            assertTrue(actionResult?.exceptionOrNull() is ClearFlowException)
            assertNull(testFlow.session.data.value)
            assertEquals(Lifecycle.State.NotInitialized, testFlow.lifecycle.value)
        }
}
