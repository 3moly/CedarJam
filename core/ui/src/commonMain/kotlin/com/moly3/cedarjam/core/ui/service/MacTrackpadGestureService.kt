package com.moly3.cedarjam.core.ui.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MacTrackpadGestureService {

    private val _valueStateFlow = MutableSharedFlow<Double>(1)

    val valueStateFlow: SharedFlow<Double> = _valueStateFlow

    suspend fun shareValue(value: Double) {
        _valueStateFlow.emit(value)
    }
}