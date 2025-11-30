package com.moly3.cedarjam.core.storage.func

import android.os.Build
import androidx.annotation.RequiresApi
import com.moly3.cedarjam.core.storage.model.OtherFileMeta
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinInstant

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalTime::class)
actual fun getOtherFileMeta(path: String): OtherFileMeta {
    val filePath = Paths.get(path)
    val attrs = Files.readAttributes(filePath, BasicFileAttributes::class.java)
    return OtherFileMeta(
        createdDateTime = attrs.creationTime().toInstant().toKotlinInstant(),
        modifiedDateTime = attrs.lastModifiedTime().toInstant().toKotlinInstant()
    )
}