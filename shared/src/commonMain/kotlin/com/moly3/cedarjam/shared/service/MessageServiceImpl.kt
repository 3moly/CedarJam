package com.moly3.cedarjam.shared.service

import com.moly3.cedarjam.core.domain.service.IMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class MessageServiceImpl : IMessageService {
    private val scope = CoroutineScope(Job())
    private val _sharedFlow = MutableSharedFlow<String?>()

    override fun sendMessage(message: String?) {
        scope.launch {
            _sharedFlow.emit(message)
        }
    }

    override fun getFlow(): SharedFlow<String?> {
        return _sharedFlow
    }
}