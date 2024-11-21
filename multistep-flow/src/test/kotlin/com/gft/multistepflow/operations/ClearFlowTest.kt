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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        testFlow.start(testStep)
        asyncUndispatchedOnUnconfinedDispatcher {
            testStep.performAction(TestFlowAction {
                onContinueAction.receive()
                lastActionTask.run()
            })
        }

        testFlow.clear()
        onContinueAction.trySend(Unit)

        verify {
            lastActionTask wasNot called
        }

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

            testFlow.start(testStep)
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(TestFlowAction {
                    testFlow.clear()
                    lastActionTask.run()
                })
            }

            verify {
                lastActionTask wasNot called
            }

            assertNull(testFlow.session.data.value)
            assertEquals(Lifecycle.State.NotInitialized, testFlow.lifecycle.value)
        }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given an Action in progress and pending actions, when MultiStepFlow_clear invoked outside the Action being performed, cancel all the actions`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow()
            val onContinueAction = Channel<Unit>()

            val pendingAction1 = spyk(TestFlowAction {
                println("#Test 1")
            })
            val pendingAction2 = spyk(TestFlowAction {
                println("#Test 2")
            })

            testFlow.start(testStep)
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(TestFlowAction {
                    onContinueAction.receive()
                })
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(pendingAction1)
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(pendingAction2)
            }

            asyncUndispatchedOnUnconfinedDispatcher {
                testFlow.clear()
            }

            onContinueAction.trySend(Unit)

            coVerify {
                pendingAction1 wasNot called
                pendingAction2 wasNot called
            }

            assertNull(testFlow.session.data.value)
            assertEquals(Lifecycle.State.NotInitialized, testFlow.lifecycle.value)
        }

    @Suppress("DeferredResultUnused")
    @Test
    fun `given an Action in progress and pending actions, when MultiStepFlow_clear invoked inside the Action being performed, cancel all the actions`(): Unit =
        runBlocking {
            val testStep = Step(TestStep)
            val testFlow = TestFlow()
            val onContinueAction = Channel<Unit>()

            val pendingAction1 = spyk(TestFlowAction {
                println("#Test 1")
            })
            val pendingAction2 = spyk(TestFlowAction {
                println("#Test 2")
            })

            testFlow.start(testStep)
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(TestFlowAction {
                    onContinueAction.receive()
                    testFlow.clear()
                })
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(pendingAction1)
            }
            asyncUndispatchedOnUnconfinedDispatcher {
                testStep.performAction(pendingAction2)
            }

            onContinueAction.send(Unit)

            coVerify {
                pendingAction1 wasNot called
                pendingAction2 wasNot called
            }

            assertNull(testFlow.session.data.value)
            assertEquals(Lifecycle.State.NotInitialized, testFlow.lifecycle.value)
        }
}