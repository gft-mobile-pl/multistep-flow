package com.gft.multistepflow.example.di

import com.gft.multistepflow.example.domain.model.LoginFlow
import com.gft.multistepflow.example.domain.usecases.AwaitLoginStepUseCase
import com.gft.multistepflow.example.domain.usecases.ClearLoginFlowErrorUseCase
import com.gft.multistepflow.example.domain.usecases.EndLoginFlowUseCase
import com.gft.multistepflow.example.domain.usecases.GetCurrentLoginStepUseCase
import com.gft.multistepflow.example.domain.usecases.GetLoginStepFromHistoryUseCase
import com.gft.multistepflow.example.domain.usecases.GoBackToLoginStepUseCase
import com.gft.multistepflow.example.domain.usecases.PerformLoginActionUseCase
import com.gft.multistepflow.example.domain.usecases.RequireLoginStepUseCase
import com.gft.multistepflow.example.domain.usecases.SetLoginStepUseCase
import com.gft.multistepflow.example.domain.usecases.StartLoginFlowUseCase
import com.gft.multistepflow.example.domain.usecases.StreamLoginFlowStateUseCase
import com.gft.multistepflow.example.domain.usecases.UpdateLoginFlowUserInputUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sealedLoginFlowDomainModule = module {
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