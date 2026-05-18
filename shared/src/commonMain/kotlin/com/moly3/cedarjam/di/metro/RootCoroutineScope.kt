package com.moly3.cedarjam.di.metro

import kotlinx.coroutines.CoroutineScope
import kotlin.jvm.JvmInline

/** Scope for [com.moly3.cedarjam.navigation.RootComponent] (navigator subscription, etc.). */
@JvmInline
value class RootCoroutineScope(val scope: CoroutineScope)
