package com.moly3.cedarjam.core.domain.service

import kotlinx.coroutines.flow.SharedFlow

interface IMessageService {
    fun sendMessage(message: String?)
    fun getFlow(): SharedFlow<String?>
}