package com.gft.multistepflow.utils

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.StepType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <FlowStepType : StepType<*, *, *, *>>MultiStepFlow<FlowStepType>.logSummary(onLogLine: (String) -> Unit): MultiStepFlow<FlowStepType> {
    CoroutineScope(Dispatchers.Unconfined).launch(start = CoroutineStart.UNDISPATCHED) {
        session.data.collect {
            onLogLine(this@logSummary.toString())
        }
    }
    return this
}

fun <FlowStepType : StepType<*, *, *, *>>MultiStepFlow<FlowStepType>.logDetails(onLogLine: (String) -> Unit): MultiStepFlow<FlowStepType> {
    CoroutineScope(Dispatchers.Unconfined).launch(start = CoroutineStart.UNDISPATCHED) {
        session.data.collect { state ->
            onLogLine("${this@logDetails::class.simpleName}(")
            onLogLine("\tisAnyOperationInProgress=${state?.isAnyOperationInProgress ?: false}")
            if (state == null) {
                onLogLine("\tsteps=[]")
            } else {
                onLogLine("\tsteps=[")
                state.stepsHistory.mapIndexed { index, step ->
                    onLogLine("\t\t$index=$step")
                }
                onLogLine("\t]")
            }
            onLogLine(")")
        }
    }
    return this
}
