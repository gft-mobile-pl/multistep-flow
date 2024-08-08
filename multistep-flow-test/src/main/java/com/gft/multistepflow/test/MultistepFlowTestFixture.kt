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

object MultistepFlowTestFixture : MultiStepFlow<StepType<*, *, *, DefaultNoOpValidator>>(historyEnabled = true) {
    private object EmptyStepType : StepType<Unit, Unit, Unit, DefaultNoOpValidator>

    init {
        start()
    }

    private fun start() = runBlocking { StartMultiStepFlowUseCase(this@MultistepFlowTestFixture)(Step(EmptyStepType)) }

    private fun end() = runBlocking { EndMultiStepFlowUseCase(this@MultistepFlowTestFixture)() }

    fun restartTestFlow() {
        end()
        start()
    }

    fun getCurrentTestFlowError() = runBlocking {
        StreamFlowStateUseCase(this@MultistepFlowTestFixture)().first().currentStep.error
    }

    suspend fun testAction(
        action: Action,
        dispatcher: CoroutineDispatcher,
        transactionId: String = UUID.randomUUID().toString(),
    ) = PerformActionUseCase(this)(action, dispatcher, transactionId)

    suspend fun testAction(
        action: Action,
        transactionId: String = UUID.randomUUID().toString(),
    ) = PerformActionUseCase(this)(action, transactionId)
}
