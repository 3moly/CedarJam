package com.moly3.cedarjam.core.domain.service

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Stable
class AlertService {

    private val _sendFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )

    val sendFlow: SharedFlow<String> = _sendFlow

    fun sendMessage(message: String) {
        _sendFlow.tryEmit(message)
    }
}