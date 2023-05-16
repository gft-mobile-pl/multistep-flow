package com.gft.multistepflow.example.domain.model

import com.gft.multistepflow.model.MultiStepFlow

class LoginFlow : MultiStepFlow<LoginStep<*, *, *, *>>(historyEnabled = true)