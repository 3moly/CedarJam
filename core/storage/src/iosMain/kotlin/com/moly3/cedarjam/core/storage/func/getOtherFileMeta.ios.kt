package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.storage.model.OtherFileMeta
import kotlinx.cinterop.*
import platform.Foundation.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
actual fun getOtherFileMeta(path: String): OtherFileMeta {
    val fileUrl = NSURL.fileURLWithPath(path)
    val resourceValues = fileUrl.resourceValuesForKeys(
        listOf(NSURLCreationDateKey, NSURLContentModificationDateKey), null
    )
    val created = (resourceValues?.get(NSURLCreationDateKey) as? NSDate)?.let {
        Instant.fromEpochMilliseconds((it.timeIntervalSince1970 * 1000).toLong())
    } ?: Instant.DISTANT_PAST

    val modified = (resourceValues?.get(NSURLContentModificationDateKey) as? NSDate)?.let {
        Instant.fromEpochMilliseconds((it.timeIntervalSince1970 * 1000).toLong())
    } ?: Instant.DISTANT_PAST

    return OtherFileMeta(
        createdDateTime = created,
        modifiedDateTime = modified,
    )
}