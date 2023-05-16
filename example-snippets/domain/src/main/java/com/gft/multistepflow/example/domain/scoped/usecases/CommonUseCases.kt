package com.gft.multistepflow.example.domain.scoped.usecases

import com.gft.multistepflow.example.domain.scoped.model.LoginFlow
import com.gft.multistepflow.example.domain.scoped.model.LoginStep
import com.gft.multistepflow.usecases.AwaitStepUseCase
import com.gft.multistepflow.usecases.ClearErrorUseCase
import com.gft.multistepflow.usecases.EndMultiStepFlow
import com.gft.multistepflow.usecases.GetCurrentStepUseCase
import com.gft.multistepflow.usecases.GetStepFromHistoryUseCase
import com.gft.multistepflow.usecases.GoBackToStepUseCase
import com.gft.multistepflow.usecases.PerformActionUseCase
import com.gft.multistepflow.usecases.RequireStepUseCase
import com.gft.multistepflow.usecases.SetStepUseCase
import com.gft.multistepflow.usecases.StartMultiStepFlow
import com.gft.multistepflow.usecases.StreamFlowStateUseCase
import com.gft.multistepflow.usecases.UpdateUserInputUseCase

internal class StartLoginFlowUseCase(loginFlow: LoginFlow) : StartMultiStepFlow<LoginStep<*, *, *, *>>(loginFlow)
class EndLoginFlowUseCase(loginFlow: LoginFlow) : EndMultiStepFlow(loginFlow)
class AwaitLoginStepUseCase(loginFlow: LoginFlow) : AwaitStepUseCase(loginFlow)
class ClearLoginFlowErrorUseCase(loginFlow: LoginFlow) : ClearErrorUseCase(loginFlow)
class GetCurrentLoginStepUseCase(loginFlow: LoginFlow) : GetCurrentStepUseCase(loginFlow)
class GetLoginStepFromHistoryUseCase(loginFlow: LoginFlow) : GetStepFromHistoryUseCase(loginFlow)
class GoBackToLoginStepUseCase(loginFlow: LoginFlow) : GoBackToStepUseCase<LoginStep<*, *, *, *>>(loginFlow)
class PerformLoginActionUseCase(loginFlow: LoginFlow) : PerformActionUseCase(loginFlow)
class RequireLoginStepUseCase(loginFlow: LoginFlow) : RequireStepUseCase(loginFlow)
class SetLoginStepUseCase(loginFlow: LoginFlow) : SetStepUseCase<LoginStep<*, *, *, *>>(loginFlow)
class StreamLoginFlowStateUseCase(loginFlow: LoginFlow) : StreamFlowStateUseCase<LoginStep<*, *, *, *>>(loginFlow)
class UpdateLoginFlowUserInputUseCase(loginFlow: LoginFlow) : UpdateUserInputUseCase(loginFlow)
