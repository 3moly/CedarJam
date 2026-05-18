package com.moly3.cedarjam.core.domain.func

import java.awt.Desktop
import java.io.File

actual suspend fun shareFile(fullPath: String) {
    val file = File(fullPath)
    if (!file.exists()) return

    val desktop = Desktop.getDesktop()

    if (desktop.isSupported(Desktop.Action.OPEN)) {
//        desktop.open(file) // opens file with default app
    }
}

actual suspend fun shareBytes(bytes: ByteArray) {
}