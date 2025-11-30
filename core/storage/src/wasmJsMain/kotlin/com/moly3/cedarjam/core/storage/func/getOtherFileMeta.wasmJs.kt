package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.storage.model.OtherFileMeta
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual fun getOtherFileMeta(path: String): OtherFileMeta {
    return OtherFileMeta(Clock.System.now(), Clock.System.now())
}