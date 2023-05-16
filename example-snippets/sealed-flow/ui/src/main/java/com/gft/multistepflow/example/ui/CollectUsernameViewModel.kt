package com.gft.multistepflow.example.ui

import androidx.lifecycle.viewModelScope
import com.gft.multistepflow.example.domain.actions.getAcceptUsernameAction
import com.gft.multistepflow.example.domain.model.LoginStep.CollectUsername
import com.gft.multistepflow.example.domain.model.Username
import com.gft.multistepflow.example.domain.usecases.AwaitLoginStepUseCase
import com.gft.multistepflow.example.domain.usecases.BeginLoginUseCase
import com.gft.multistepflow.example.domain.usecases.EndLoginFlowUseCase
import com.gft.multistepflow.example.domain.usecases.PerformLoginActionUseCase
import com.gft.multistepflow.example.domain.usecases.StreamLoginFlowStateUseCase
import com.gft.multistepflow.example.domain.usecases.UpdateLoginFlowUserInputUseCase
import com.gft.multistepflow.example.domain.utils.launchUndispatched
import com.gft.multistepflow.example.ui.ProvideUsernameNavigationEffect.NavigateToNextScreen
import com.gft.multistepflow.utils.filterByStepType
import com.gft.mvi.BaseMviViewModel
import com.gft.mvi.ViewEffect
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class CollectUsernameViewModel(
    beginLogin: BeginLoginUseCase,
    private val endLoginFlow: EndLoginFlowUseCase,
    private val performAction: PerformLoginActionUseCase,
    private val updateUserInput: UpdateLoginFlowUserInputUseCase,
    private val awaitStepUseCase: AwaitLoginStepUseCase,
    private val streamFlowState: StreamLoginFlowStateUseCase
) : BaseMviViewModel<ProvideUsernameViewState, ProvideUsernameViewEvent, ProvideUsernameNavigationEffect, ViewEffect>(
    ProvideUsernameViewState(username = "", isLoadingIndicatorVisible = false)
) {
    init {
        viewModelScope.launchUndispatched {
            beginLogin()
        }
    }

    override val viewStates: StateFlow<ProvideUsernameViewState> = streamFlowState()
        .onEach { state ->
            if (state.currentStep.type != CollectUsername) {
                val destinationId = state.currentStep.resolveDestinationId()
                // or
                val alternativelyObtainedDestinationId = state.currentStep.type.resolveDestinationId()

                dispatchNavigationEffect(NavigateToNextScreen(destinationId))
            }
        }
        .filterByStepType(CollectUsername)
        .map { state ->
            ProvideUsernameViewState(
                username = state.currentStep.userInput.value,
                isLoadingIndicatorVisible = state.isAnyOperationInProgress
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ProvideUsernameViewState("", false))

    override fun onEvent(event: ProvideUsernameViewEvent) {
        when (event) {
            ProvideUsernameViewEvent.OnBackClicked -> {
                viewModelScope.launchUndispatched {
                    viewState = viewState.copy(isLoadingIndicatorVisible = true)
                    withContext(NonCancellable) {
                        endLoginFlow()
                    }
                    dispatchNavigationEffect(ProvideUsernameNavigationEffect.NavigateBack)
                }
            }

            ProvideUsernameViewEvent.OnNextClicked -> {
                viewModelScope.launchUndispatched {
                    val step = awaitStepUseCase(CollectUsername)
                    performAction(step.actions.getAcceptUsernameAction())
                }
            }

            is ProvideUsernameViewEvent.OnUsernameChanged -> updateUserInput(CollectUsername) {
                Username(event.username)
            }
        }
    }
}