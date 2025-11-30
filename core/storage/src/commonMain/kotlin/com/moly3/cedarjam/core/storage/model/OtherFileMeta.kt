package com.moly3.cedarjam.core.storage.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class OtherFileMeta @OptIn(ExperimentalTime::class) constructor(
    val createdDateTime: Instant,
    val modifiedDateTime: Instant,
)