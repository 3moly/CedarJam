package com.moly3.cedarjam.core.storage.func

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime

actual fun setLastWriteTimeUtc(fullpath: String, modifiedTime: Long) {
    val filePath = Paths.get(fullpath)
    val fileTime = FileTime.fromMillis(modifiedTime)
    Files.setLastModifiedTime(filePath, fileTime)
}