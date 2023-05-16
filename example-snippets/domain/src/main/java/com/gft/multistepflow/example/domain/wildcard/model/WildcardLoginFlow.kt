package com.gft.multistepflow.example.domain.wildcard.model

import com.gft.multistepflow.model.MultiStepFlow
import com.gft.multistepflow.model.StepType

class WildcardLoginFlow : MultiStepFlow<StepType<*, *, *, *>>(false)