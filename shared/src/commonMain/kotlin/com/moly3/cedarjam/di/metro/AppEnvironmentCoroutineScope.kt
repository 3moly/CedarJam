package com.moly3.cedarjam.di.metro

import kotlinx.coroutines.CoroutineScope

/** Long-running scope used by [com.moly3.cedarjam.core.data.AppEnvironment]. */
@JvmInline
value class AppEnvironmentCoroutineScope(val scope: CoroutineScope)
