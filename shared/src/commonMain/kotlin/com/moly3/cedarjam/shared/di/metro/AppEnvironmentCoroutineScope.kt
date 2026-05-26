package com.moly3.cedarjam.shared.di.metro

import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.JvmInline

/** Long-running scope used by [com.moly3.cedarjam.core.data.AppEnvironment]. */

@JvmInline
value class AppEnvironmentCoroutineScope(val scope: CoroutineScope)
