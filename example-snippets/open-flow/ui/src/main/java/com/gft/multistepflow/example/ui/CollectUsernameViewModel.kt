package com.gft.multistepflow.example.ui

import androidx.lifecycle.viewModelScope
import com.gft.multistepflow.example.domain.actions.getAcceptUsernameAction
import com.gft.multistepflow.example.domain.model.CollectUsername
import com.gft.multistepflow.example.domain.model.Username
import com.gft.multistepflow.example.domain.usecases.BeginLoginUseCase
import com.gft.multistepflow.example.domain.utils.launchUndispatched
import com.gft.multistepflow.example.ui.ProvideUsernameNavigationEffect.NavigateBack
import com.gft.multistepflow.example.ui.ProvideUsernameNavigationEffect.NavigateToNextScreen
import com.gft.multistepflow.model.StepType
import com.gft.multistepflow.usecases.AwaitStepUseCase
import com.gft.multistepflow.usecases.EndMultiStepFlowUseCase
import com.gft.multistepflow.usecases.PerformActionUseCase
import com.gft.multistepflow.usecases.StreamFlowStateUseCase
import com.gft.multistepflow.usecases.UpdateUserInputUseCase
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
    private val beginLogin: BeginLoginUseCase,
    private val endLoginFlow: EndMultiStepFlowUseCase,
    private val performAction: PerformActionUseCase,
    private val updateUserInput: UpdateUserInputUseCase,
    private val awaitStepUseCase: AwaitStepUseCase,
    streamFlowState: StreamFlowStateUseCase<StepType<*, *, *, *>>
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
                val destinationId = 0 // destinationResolver(state.currentStep)
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

    // The code below shows ARB SME-style view state updates and navigation effects updates.
    // The main disadvantage of this approach is that VMs subscription are active even if the corresponding screen is in the backstack.
    //
    // init {
    //     viewModelScope.launchUndispatched {
    //         beginLogin()
    //     }
    //     startViewStateUpdates()
    //     startNavigationEffectUpdates()
    // }
    //
    // private fun startViewStateUpdates() = viewModelScope.launchUndispatched {
    //     streamFlowState()
    //         .filterByStepType(CollectUsername)
    //         .onEach { state ->
    //             viewState = viewState.copy(
    //                 username = state.currentStep.userInput.value
    //             )
    //         }
    //         .collect()
    // }
    //
    // private fun startNavigationEffectUpdates() = viewModelScope.launchUndispatched {
    //     streamFlowState()
    //         .collect { state ->
    //             val destinationId = 0 // destinationResolver(state.currentStep)
    //             dispatchNavigationEffect(NavigateToNextScreen(destinationId))
    //         }
    // }

}