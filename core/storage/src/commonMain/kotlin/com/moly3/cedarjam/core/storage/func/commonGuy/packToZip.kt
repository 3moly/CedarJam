package com.moly3.cedarjam.core.storage.func.commonGuy

expect suspend fun packToZip(
    workspaceFolderAbsolutePath: String,
    filesToArchive: List<String>,
    archivePath: String,
)