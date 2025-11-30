package com.moly3.cedarjam.core.storage.func

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime

@RequiresApi(Build.VERSION_CODES.O)
actual fun setLastWriteTimeUtc(fullpath: String, modifiedTime: Long) {
    val filePath = Paths.get(fullpath)
    val fileTime = FileTime.fromMillis(modifiedTime)
    Files.setLastModifiedTime(filePath, fileTime)
    val sd = File(fullpath)
    val lastTime = sd.lastModified()
    assert(lastTime == modifiedTime)
}