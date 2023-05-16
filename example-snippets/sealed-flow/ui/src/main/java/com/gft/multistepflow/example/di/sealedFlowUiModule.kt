package com.gft.multistepflow.example.di

import com.gft.multistepflow.example.ui.CollectUsernameViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

fun sealedLoginFlowUiModule() = module {
    viewModelOf(::CollectUsernameViewModel)
}