package com.gft.multistepflow.example.ui

import com.gft.mvi.NavigationEffect
import com.gft.mvi.ViewEvent
import com.gft.mvi.ViewState

data class ProvideUsernameViewState(
    val username: String,
    val isLoadingIndicatorVisible: Boolean
) : ViewState

sealed interface ProvideUsernameViewEvent : ViewEvent {
    data class OnUsernameChanged(val username: String) : ProvideUsernameViewEvent
    object OnNextClicked : ProvideUsernameViewEvent
    object OnBackClicked : ProvideUsernameViewEvent
}

sealed interface ProvideUsernameNavigationEffect : NavigationEffect {
    data class NavigateToNextScreen(val destinationId: Int) : ProvideUsernameNavigationEffect
    object NavigateBack : ProvideUsernameNavigationEffect
}