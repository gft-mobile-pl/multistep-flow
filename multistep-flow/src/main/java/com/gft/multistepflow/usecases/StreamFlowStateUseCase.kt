package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.FlowState
import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.filterByStepType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

open class StreamFlowStateUseCase<FlowStepType : StepType<*, *, *, *>>(private val flow: MultiStepFlow<FlowStepType>) {
    operator fun invoke(): Flow<FlowState<FlowStepType, *, *, *>> = flow
        .session
        .data
        .filterNotNull()
        .map {
            @Suppress("UNCHECKED_CAST")
            it as FlowState<FlowStepType, *, *, *>
        }
        .distinctUntilChanged()

    operator fun <T : StepType<out Payload, out UserInput, out ValidationResult, *>, Payload, UserInput, ValidationResult> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Flow<FlowState<T, out Payload, out UserInput, out ValidationResult>> {
        return flow.session.data
            .filterNotNull()
            .filterByStepType(stepType, *stepTypes)
    }
}