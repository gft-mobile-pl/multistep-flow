package com.gft.multistepflow.example.ui.wildcard

import androidx.lifecycle.viewModelScope
import com.gft.multistepflow.example.domain.wildcard.actions.getAcceptUsernameAction
import com.gft.multistepflow.example.domain.wildcard.model.CollectUsername
import com.gft.multistepflow.example.domain.wildcard.model.Username
import com.gft.multistepflow.example.domain.wildcard.usecases.BeginLoginUseCase
import com.gft.multistepflow.example.domain.utils.launchUndispatched
import com.gft.multistepflow.example.ui.wildcard.ProvideUsernameNavigationEffect.NavigateBack
import com.gft.multistepflow.example.ui.wildcard.ProvideUsernameNavigationEffect.NavigateToNextScreen
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.AwaitStepUseCase
import com.gft.multistepflow.usecases.EndMultiStepFlow
import com.gft.multistepflow.usecases.PerformActionUseCase
import com.gft.multistepflow.usecases.StreamFlowStateUseCase
import com.gft.multistepflow.usecases.UpdateUserInputUseCase
import com.gft.multistepflow.utils.filterByStepType
import com.gft.mvi.BaseMviViewModel
import com.gft.mvi.ViewEffect
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class CollectUsernameViewModel(
    private val beginLogin: BeginLoginUseCase,
    private val endLoginFlow: EndMultiStepFlow,
    private val performAction: PerformActionUseCase,
    private val updateUserInput: UpdateUserInputUseCase,
    private val awaitStepUseCase: AwaitStepUseCase,
    private val streamFlowState: StreamFlowStateUseCase<StepType<*, *, *, *>>
) : BaseMviViewModel<ProvideUsernameViewState, ProvideUsernameViewEvent, ProvideUsernameNavigationEffect, ViewEffect>(
    ProvideUsernameViewState(username = "", isLoadingIndicatorVisible = false)
) {
    init {
        viewModelScope.launchUndispatched {
            beginLogin()
        }
        startViewStateUpdates()
        startNavigationEffectUpdates()
    }

    private fun startViewStateUpdates() = viewModelScope.launchUndispatched {
        streamFlowState()
            .filterByStepType(CollectUsername)
            .onEach { state ->
                viewState = viewState.copy(
                    username = state.currentStep.userInput.value
                )
            }
            .collect()
    }

    private fun startNavigationEffectUpdates() = viewModelScope.launchUndispatched {
        streamFlowState()
            .collect { state ->
                val destinationId = 0 // destinationResolver(state.currentStep)
                dispatchNavigationEffect(NavigateToNextScreen(destinationId))
            }
    }

    // override val viewStates: StateFlow<ProvideUsernameViewState> = streamFlowState()
    //     .onEach { state ->
    //         if (state.currentStep.type != CollectUsername) {
    //             val destinationId = 0 // destinationResolver(state.currentStep)
    //             dispatchNavigationEffect(NavigateToNextScreen(destinationId))
    //         }
    //     }
    //     .filterByStepType(CollectUsername)
    //     .map { state ->
    //         ProvideUsernameViewState(
    //             username = state.currentStep.userInput.value,
    //             isLoadingIndicatorVisible = state.isAnyOperationInProgress
    //         )
    //     }
    //     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ProvideUsernameViewState("", false))

    override fun onEvent(event: ProvideUsernameViewEvent) {
        when (event) {
            ProvideUsernameViewEvent.OnBackClicked -> {
                viewModelScope.launchUndispatched {
                    viewState = viewState.copy(isLoadingIndicatorVisible = true)
                    withContext(NonCancellable) {
                        endLoginFlow()
                    }
                    dispatchNavigationEffect(NavigateBack)
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