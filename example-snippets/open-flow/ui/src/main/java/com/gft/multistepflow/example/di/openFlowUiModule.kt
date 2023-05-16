package com.gft.multistepflow.example.di

import com.gft.multistepflow.example.ui.CollectUsernameViewModel
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