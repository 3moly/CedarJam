package com.moly3.cedarjam.core.domain.func

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

expect fun <T> runBlocking(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T