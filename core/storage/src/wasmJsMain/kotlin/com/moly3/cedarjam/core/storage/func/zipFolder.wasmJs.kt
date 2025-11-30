package com.moly3.cedarjam.core.storage.func


actual fun zipFolder(source: String, archive: String, relativePathsToSkip: List<String>) {
}

actual suspend fun zipFolder(
    workspaceFolderAbsolutePath: String,
    filesToArchive: List<String>,
    archive: String
) {
}