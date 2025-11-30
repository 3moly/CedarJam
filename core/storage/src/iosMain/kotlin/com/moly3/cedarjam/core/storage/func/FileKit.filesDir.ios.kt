package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir

actual fun FileKit.filesDirPath(): String {
    return FileKit.filesDir.toString()
}

actual fun FileKit.init(androidApplicationContext: AndroidApplicationContext) {
}