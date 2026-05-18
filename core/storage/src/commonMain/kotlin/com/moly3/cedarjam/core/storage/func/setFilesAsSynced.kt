package com.moly3.cedarjam.core.storage.func


import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.indexdb.IndexDatabase
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun setFilesAsSynced(
    paths: List<String>,
    serverFiles: List<FileItem>,
    dbHelper: IndexDatabase
) {
    val serverFilesMap = serverFiles.associateBy { it.relativePath.normalizeText() }

    dbHelper.transaction {
        paths.forEach { relativePathStr ->
            val normalizedPath = relativePathStr.normalizeText()
            val serverFile = serverFilesMap[normalizedPath] ?: return@forEach

            dbHelper.indexFileQueries.insertItem(
                relativePath = relativePathStr,
                contentHash = serverFile.contentHash,
                modifiedTime = serverFile.modifiedTime,
                size = serverFile.size,
                isDirectory = if (serverFile.isDirectory) 1L else 0L,
                lastSyncedHash = serverFile.contentHash,
                serverSyncStatus = SyncStatus.SYNCED.code
            )
        }
    }
}