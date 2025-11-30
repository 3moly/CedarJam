package com.moly3.cedarjam.core.domain.func

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun nowInMs(): Long {
    return Clock.System.now().toEpochMilliseconds()
}