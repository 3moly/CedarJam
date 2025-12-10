package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.AppId
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import io.github.vinceglb.filekit.FileKit
import kotlinx.io.files.Path

actual fun FileKit.filesDirPath(): String {
    return ""
}

actual fun FileKit.init(androidApplicationContext: AndroidApplicationContext) {
    FileKit.init(AppId)
}