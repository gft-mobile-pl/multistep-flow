package com.gft.multistepflow.example.openflow.di

import com.gft.multistepflow.example.openflow.di.OpenLoginFlowQualifier
import com.gft.multistepflow.example.openflow.ui.CollectUsernameViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun openLoginFlowUiModule() = module {
    viewModel {
        CollectUsernameViewModel(
            get(OpenLoginFlowQualifier),
            get(OpenLoginFlowQualifier),
            get(OpenLoginFlowQualifier),
            get(OpenLoginFlowQualifier),
            get(OpenLoginFlowQualifier),
            get(OpenLoginFlowQualifier)
        )
    }
}