package com.moly3.cedarjam.core.storage.func

import kotlinx.cinterop.*
import kotlinx.cinterop.get
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
actual fun setLastWriteTimeUtc(fullpath: String, modifiedTime: Long) {
    memScoped {
        val timeBuffer = allocArray<timeval>(2)
        // Access time (index 0) — leave unchanged by setting 0
        timeBuffer[0].tv_sec = 0
        timeBuffer[0].tv_usec = 0
        // Modification time (index 1)
        timeBuffer[1].tv_sec = (modifiedTime / 1000).convert()
        timeBuffer[1].tv_usec = ((modifiedTime % 1000) * 1000).convert()

        utimes(fullpath, timeBuffer)
    }
}