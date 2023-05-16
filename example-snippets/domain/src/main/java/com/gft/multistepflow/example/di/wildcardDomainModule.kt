package com.gft.multistepflow.example.di

import com.gft.multistepflow.example.domain.wildcard.actions.AcceptUsername
import com.gft.multistepflow.example.domain.wildcard.model.WildcardLoginFlow
import com.gft.multistepflow.example.domain.wildcard.usecases.BeginLoginUseCase
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.AwaitStepUseCase
import com.gft.multistepflow.usecases.EndMultiStepFlow
import com.gft.multistepflow.usecases.GetCurrentStepUseCase
import com.gft.multistepflow.usecases.PerformActionUseCase
import com.gft.multistepflow.usecases.RequireStepUseCase
import com.gft.multistepflow.usecases.SetStepUseCase
import com.gft.multistepflow.usecases.StartMultiStepFlow
import com.gft.multistepflow.usecases.StreamFlowStateUseCase
import com.gft.multistepflow.usecases.UpdateUserInputUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val WildcardLoginFlowQualifier = named("WildcardLoginFlowQualifier")

val wildcardLoginFlowDomainModule = module {
    single(WildcardLoginFlowQualifier) { WildcardLoginFlow() }

    factory { BeginLoginUseCase(get(WildcardLoginFlowQualifier)) }

    factory(WildcardLoginFlowQualifier) { StartMultiStepFlow<StepType<*, *, *, *>>(get(WildcardLoginFlowQualifier)) }
    factory(WildcardLoginFlowQualifier) { EndMultiStepFlow(get(WildcardLoginFlowQualifier)) }

    factory(WildcardLoginFlowQualifier) { StreamFlowStateUseCase<StepType<*, *, *, *>>(get(WildcardLoginFlowQualifier)) }
    factory(WildcardLoginFlowQualifier) { RequireStepUseCase(get(WildcardLoginFlowQualifier)) }
    factory(WildcardLoginFlowQualifier) { AwaitStepUseCase(get(WildcardLoginFlowQualifier)) }
    factory(WildcardLoginFlowQualifier) { GetCurrentStepUseCase<StepType<*, *, *, *>>(get(WildcardLoginFlowQualifier)) }
    factory(WildcardLoginFlowQualifier) { SetStepUseCase<StepType<*, *, *, *>>(get(WildcardLoginFlowQualifier)) }
    factory(WildcardLoginFlowQualifier) { PerformActionUseCase(get(WildcardLoginFlowQualifier)) }
    factory(WildcardLoginFlowQualifier) { UpdateUserInputUseCase(get(WildcardLoginFlowQualifier)) }

    factory { AcceptUsername(get(WildcardLoginFlowQualifier), get(WildcardLoginFlowQualifier)) }
}
