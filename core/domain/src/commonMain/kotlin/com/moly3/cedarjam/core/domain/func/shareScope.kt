package com.moly3.cedarjam.core.domain.func

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

fun <T> Flow<T>.shareScope(scope: CoroutineScope): Flow<T> {
    return this.shareIn(
        scope = scope,
        started = SharingStarted.Lazily,
        replay = 1
    )
}