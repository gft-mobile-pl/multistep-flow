package com.gft.multistepflow.usecases

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.Step
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.utils.castOrNull

open class GetStepFromHistoryUseCase(private val flow: MultiStepFlow<*>) {

    operator fun invoke(step: Step<*, *, *, *, *>) = flow.session.requireData().stepsHistory.lastOrNull { stepFromHistory ->
        step == stepFromHistory
    } ?: throw IllegalArgumentException("Provided step was not found in flow history")

    operator fun <T : StepType<out Payload, out UserInput, out ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, out ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }

    @JvmName("invoke_Same_Payload")
    operator fun <T : StepType<Payload, out UserInput, out ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, out UserInput, out ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }

    @JvmName("invoke_Same_Payload_UserInput")
    operator fun <T : StepType<Payload, UserInput, out ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, UserInput, out ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }

    @JvmName("invoke_Same_Payload_ValidationResult")
    operator fun <T : StepType<Payload, out UserInput, ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, out UserInput, ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }

    @JvmName("invoke_Same_Payload_UserInput_ValidationResult")
    operator fun <T : StepType<Payload, UserInput, ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, Payload, UserInput, ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }

    @JvmName("invoke_Same_UserInput")
    operator fun <T : StepType<out Payload, UserInput, out ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, UserInput, out ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }

    @JvmName("invoke_Same_UserInput_ValidationResult")
    operator fun <T : StepType<out Payload, UserInput, ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, UserInput, ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }

    @JvmName("invoke_Same_ValidationResult")
    operator fun <T : StepType<out Payload, out UserInput, ValidationData, *>, Payload, UserInput, ValidationData> invoke(
        stepType: T,
        vararg stepTypes: T
    ): Step<T, out Payload, out UserInput, ValidationData, *> {
        return flow.session.requireData().stepsHistory.mapNotNull { stepFromHistory ->
            stepFromHistory.castOrNull(stepType, *stepTypes)
        }.lastOrNull() ?: throw IllegalArgumentException("Provided step type was not found in flow history")
    }
}
