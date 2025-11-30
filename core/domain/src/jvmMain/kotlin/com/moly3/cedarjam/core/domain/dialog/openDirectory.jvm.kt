package com.moly3.cedarjam.core.domain.dialog

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker

actual suspend fun openDirectory(): PlatformFile? {
    return FileKit.openDirectoryPicker()
}