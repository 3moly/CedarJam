package com.moly3.cedarjam.core.domain.func

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.shareFile


actual suspend fun shareFile(fullPath: String) {
    FileKit.shareFile(PlatformFile(fullPath))
}

actual suspend fun shareBytes(bytes: ByteArray) {
}

