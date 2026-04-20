package com.moly3.cedarjam.navigation

import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.JvmInline

/** Single app-wide scope used by [NavigatorImpl]. */
@JvmInline
value class NavigatorCoroutineScope(val scope: CoroutineScope)
