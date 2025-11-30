package com.moly3.cedarjam.core.storage.func

expect fun zipFolder(source: String, archive: String, relativePathsToSkip: List<String>)
expect suspend fun zipFolder(
    workspaceFolderAbsolutePath: String,
    filesToArchive: List<String>,
    archive: String,
)