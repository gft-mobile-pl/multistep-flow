package com.gft.multistepflow.example.di

import com.gft.multistepflow.example.domain.scoped.model.LoginFlow
import com.gft.multistepflow.example.domain.scoped.usecases.AwaitLoginStepUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.ClearLoginFlowErrorUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.EndLoginFlowUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.GetCurrentLoginStepUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.GetLoginStepFromHistoryUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.GoBackToLoginStepUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.PerformLoginActionUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.RequireLoginStepUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.SetLoginStepUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.StartLoginFlowUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.StreamLoginFlowStateUseCase
import com.gft.multistepflow.example.domain.scoped.usecases.UpdateLoginFlowUserInputUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val scopedLoginFlowDomainModule = module {
    singleOf(::LoginFlow)
    factoryOf(::StartLoginFlowUseCase)
    factoryOf(::EndLoginFlowUseCase)
    factoryOf(::AwaitLoginStepUseCase)
    factoryOf(::ClearLoginFlowErrorUseCase)
    factoryOf(::GetCurrentLoginStepUseCase)
    factoryOf(::GetLoginStepFromHistoryUseCase)
    factoryOf(::GoBackToLoginStepUseCase)
    factoryOf(::PerformLoginActionUseCase)
    factoryOf(::RequireLoginStepUseCase)
    factoryOf(::SetLoginStepUseCase)
    factoryOf(::StreamLoginFlowStateUseCase)
    factoryOf(::UpdateLoginFlowUserInputUseCase)


}