package com.gft.multistepflow.example.di

import com.gft.multistepflow.example.domain.actions.AcceptUsernameAction
import com.gft.multistepflow.example.domain.model.WildcardLoginFlow
import com.gft.multistepflow.example.domain.usecases.BeginLoginUseCase
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.AwaitStepUseCase
import com.gft.multistepflow.usecases.EndMultiStepFlowUseCase
import com.gft.multistepflow.usecases.GetCurrentStepUseCase
import com.gft.multistepflow.usecases.PerformActionUseCase
import com.gft.multistepflow.usecases.RequireStepUseCase
import com.gft.multistepflow.usecases.SetStepUseCase
import com.gft.multistepflow.usecases.StartMultiStepFlowUseCase
import com.gft.multistepflow.usecases.StreamFlowStateUseCase
import com.gft.multistepflow.usecases.UpdateUserInputUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val OpenLoginFlowQualifier = named("OpenLoginFlowQualifier")

val openLoginFlowDomainModule = module {
    // flow
    single(OpenLoginFlowQualifier) { WildcardLoginFlow() }

    // out-of-the-box use cases (they need to be injected with an appropriate flow)
    factory(OpenLoginFlowQualifier) { StartMultiStepFlowUseCase<StepType<*, *, *, *>>(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { EndMultiStepFlowUseCase(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { StreamFlowStateUseCase<StepType<*, *, *, *>>(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { RequireStepUseCase(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { AwaitStepUseCase(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { GetCurrentStepUseCase<StepType<*, *, *, *>>(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { SetStepUseCase<StepType<*, *, *, *>>(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { PerformActionUseCase(get(OpenLoginFlowQualifier)) }
    factory(OpenLoginFlowQualifier) { UpdateUserInputUseCase(get(OpenLoginFlowQualifier)) }

    // the only custom use case that is required
    factory { BeginLoginUseCase(get(OpenLoginFlowQualifier)) }

    // actions
    factory { AcceptUsernameAction(get(OpenLoginFlowQualifier), get(OpenLoginFlowQualifier)) }
}
