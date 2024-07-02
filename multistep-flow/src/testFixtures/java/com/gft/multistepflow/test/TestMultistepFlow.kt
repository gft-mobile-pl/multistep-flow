package com.gft.multistepflow.test

import com.gft.multistepflow.model.Action
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.EndMultiStepFlowUseCase
import com.gft.multistepflow.usecases.PerformActionUseCase
import com.gft.multistepflow.usecases.StartMultiStepFlowUseCase
import com.gft.multistepflow.usecases.StreamFlowStateUseCase
import com.gft.multistepflow.validators.DefaultNoOpValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.UUID

private object MockStepType : StepType<Unit, Unit, Unit, DefaultNoOpValidator>

private object MockMultistepFlow : MultiStepFlow<MockStepType>(historyEnabled = true) {
    private val mockStep = Step(MockStepType)
    val startFlow = suspend {
        StartMultiStepFlowUseCase(this)(mockStep)
    }
    val testPerformAction = PerformActionUseCase(this)
    val endTestFlow = EndMultiStepFlowUseCase(this)
    val error
        get() = runBlocking { StreamFlowStateUseCase(MockMultistepFlow)().first().currentStep.error }

    init {
        runBlocking { startFlow() }
    }
}

object TestMultistepFlow {
    suspend fun performAction(action: Action, dispatcher: CoroutineDispatcher, transactionId: String = UUID.randomUUID().toString()) {
        MockMultistepFlow.testPerformAction(action, dispatcher, transactionId)
    }

    suspend fun performAction(action: Action, transactionId: String = UUID.randomUUID().toString()) {
        MockMultistepFlow.testPerformAction(action, transactionId)
    }

    fun getFlowError() = MockMultistepFlow.error

    fun restartTestFlow() = runBlocking {
        MockMultistepFlow.endTestFlow()
        MockMultistepFlow.startFlow()
    }
}
