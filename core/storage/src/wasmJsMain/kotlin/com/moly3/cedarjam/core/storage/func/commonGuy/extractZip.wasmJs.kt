package com.moly3.cedarjam.core.storage.func.commonGuy

import com.moly3.cedarjam.core.domain.model.FileItem

actual suspend fun extractZip(
    archivePath: String,
    workspaceFullPath: String,
    serverFiles: List<FileItem>
) {
}