package com.gft.multistepflow.operations

class AnotherActionInProgressException : IllegalStateException("Only one Action can be performed at the same time.")