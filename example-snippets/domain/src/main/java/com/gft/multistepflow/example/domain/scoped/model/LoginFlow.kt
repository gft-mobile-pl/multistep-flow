package com.gft.multistepflow.example.domain.scoped.model

import com.gft.multistepflow.model.MultiStepFlow

class LoginFlow : MultiStepFlow<LoginStep<*, *, *, *>>(historyEnabled = true)