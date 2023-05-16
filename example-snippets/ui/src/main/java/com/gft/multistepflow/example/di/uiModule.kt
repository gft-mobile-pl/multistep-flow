package com.gft.multistepflow.example.di

import com.gft.multistepflow.example.di.WildcardLoginFlowQualifier
import com.gft.multistepflow.example.ui.wildcard.CollectUsernameViewModel
import org.koin.dsl.module

fun wildcardLoginFlowUiModule() = module {
    factory {
        CollectUsernameViewModel(
            get(WildcardLoginFlowQualifier),
            get(WildcardLoginFlowQualifier),
            get(WildcardLoginFlowQualifier),
            get(WildcardLoginFlowQualifier),
            get(WildcardLoginFlowQualifier),
            get(WildcardLoginFlowQualifier)
        )
    }
}