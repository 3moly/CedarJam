package com.moly3.cedarjam.core.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val io: CoroutineDispatcher
    get() = Dispatchers.IO