package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.indexdb.IndexDatabase

fun syncAllFiles(
    dbHelper: IndexDatabase,
    specificIndexes: List<IndexFileDto> = listOf()
) {
    dbHelper.indexFileQueries.transaction {
        if (specificIndexes.isNotEmpty()) {
            dbHelper.indexFileQueries.transaction {
                for (item in specificIndexes) {
                    dbHelper.indexFileQueries.updateStatus(
                        relativePath = item.relativePath,
                        serverSyncStatus = SyncStatus.SYNCED.code
                    )
                }
            }
        } else {
            val dbQueries = dbHelper.indexFileQueries.selectAll().executeAsList()
            dbHelper.indexFileQueries.transaction {
                for (item in dbQueries) {
                    dbHelper.indexFileQueries.updateStatus(
                        relativePath = item.relativePath,
                        serverSyncStatus = SyncStatus.SYNCED.code
                    )
                }
            }
        }
    }
}

fun updateIndexLocal(
    localNodes: List<FileTreeNode>,
    dbHelper: IndexDatabase
) {
    dbHelper.indexFileQueries.transaction {
        val dbQueries = dbHelper.indexFileQueries
        val onDiskFiles = localNodes.flattenToMap()
        val dbRecords = dbQueries.selectAll().executeAsList().associateBy { it.relativePath }
        onDiskFiles.forEach { (path, localNode) ->
            val dbRecord = dbRecords[path]
            if (dbRecord == null) {
                val currentHash = calculateHash(localNode)
                val status = SyncStatus.NEW
                dbQueries.insertItem(
                    relativePath = path,
                    contentHash = currentHash,
                    modifiedTime = localNode.modifiedTime,
                    size = localNode.fileSize,
                    isDirectory = if (localNode.isDirectory()) 1L else 0L,
                    lastSyncedHash = null,
                    serverSyncStatus = status.code
                )
            } else {

                if (dbRecord.modifiedTime != localNode.modifiedTime) {
                    val currentHash = calculateHash(localNode)
                    val status = if (localNode.isDirectory() && dbRecord.isDirectory == 1L) {
                        SyncStatus.SYNCED
                    } else {
                        SyncStatus.DIRTY
                    }
                    dbQueries.insertItem(
                        relativePath = path,
                        contentHash = currentHash,
                        modifiedTime = localNode.modifiedTime,
                        size = localNode.fileSize,
                        isDirectory = if (localNode.isDirectory()) 1L else 0L,
                        lastSyncedHash = null,
                        serverSyncStatus = status.code
                    )
                }
            }
        }
    }
}

fun updateIndex(
    localNodes: List<FileTreeNode>,
    serverNodes: List<FileItem>,
    dbHelper: IndexDatabase
) {
    dbHelper.indexFileQueries.transaction {
        val dbQueries = dbHelper.indexFileQueries

        val onDiskFiles = localNodes.flattenToMap()
        val serverFiles = serverNodes.associateBy { it.relativePath }
        // FIX: Убрали фильтр, берем все записи
        val dbRecords = dbQueries.selectAll().executeAsList().associateBy { it.relativePath }

        // --- PHASE A: Обработка файлов на диске ---
        onDiskFiles.forEach { (path, localNode) ->
            val dbRecord = dbRecords[path]
            val serverRecord = serverFiles[path]

            val isDirectory = if (localNode.isDirectory()) 1L else 0L

            if (dbRecord == null) {
                // CASE 1: Файл действительно новый (нет в БД)
                val currentHash = calculateHash(localNode)

                val status = if (serverRecord != null && serverRecord.contentHash == currentHash) {
                    SyncStatus.SYNCED
                } else {
                    SyncStatus.NEW
                }

                dbQueries.insertItem(
                    relativePath = path,
                    contentHash = currentHash,
                    modifiedTime = localNode.modifiedTime,
                    size = localNode.fileSize,
                    isDirectory = isDirectory,
                    lastSyncedHash = serverRecord?.contentHash,
                    serverSyncStatus = status.code
                )
            } else {
                // CASE 2: Файл уже есть в БД

                // 2.1 "Призрачное" удаление (в БД удален, на диске есть -> Восстановление/Изменение)
                if (dbRecord.serverSyncStatus == SyncStatus.DELETED.code) {
                    val currentHash = calculateHash(localNode)
                    dbQueries.updateItem(
                        contentHash = currentHash,
                        modifiedTime = localNode.modifiedTime,
                        size = localNode.fileSize,
                        serverSyncStatus = SyncStatus.DIRTY.code,
                        relativePath = path
                    )
                } else {  // 2.2 Стандартная проверка
                    val isModified = localNode.modifiedTime != dbRecord.modifiedTime ||
                            localNode.fileSize != dbRecord.size

                    if (dbRecord.isDirectory == 1L) {

                    }

                    if (isModified) {
                        // Файл изменился локально
                        val newHash = calculateHash(localNode)

                        val status = if (newHash.cleanToNullIfEmpty() ==
                            dbRecord.lastSyncedHash.cleanToNullIfEmpty()
                        ) {
                            if (serverRecord == null)
                                SyncStatus.NEW
                            else
                                SyncStatus.SYNCED
                        } else {
                            if (dbRecord.isDirectory == 1L &&
                                localNode.isDirectory() &&
                                serverRecord?.isDirectory == true
                            ) {
                                SyncStatus.SYNCED
                            } else {
                                SyncStatus.DIRTY
                            }
                        }

                        dbQueries.updateItem(
                            contentHash = newHash,
                            modifiedTime = localNode.modifiedTime,
                            size = localNode.fileSize,
                            serverSyncStatus = status.code,
                            relativePath = path
                        )
                    } else {
                        // Файл на диске полностью совпадает с БД (по времени и размеру).

                        // ---> НОВАЯ ЛОГИКА (Data Loss Recovery) <---
                        // Если мы думаем, что файл SYNCED, но на сервере о нем нет записей (serverRecord == null),
                        // значит сервер потерял файл. Нужно пометить его как NEW для повторной загрузки.
                        // Важно: serverNodes должен содержать ВСЕ файлы, включая удаленные (Tombstones).
                        // Если serverRecord == null, значит нет даже Tombstone.

                        if (dbRecord.serverSyncStatus == SyncStatus.SYNCED.code && serverRecord == null) {
                            dbQueries.updateStatus(SyncStatus.NEW.code, path)
                        }
                    }
                }
            }
        }

        // --- PHASE B: Обработка удалений с диска ---
        dbRecords.keys.forEach { path ->
            if (!onDiskFiles.containsKey(path)) {
                val dbRecord = dbRecords[path]!!

                if (dbRecord.serverSyncStatus == SyncStatus.NEW.code) {
                    dbQueries.deleteItem(path)
                } else if (dbRecord.serverSyncStatus != SyncStatus.DELETED.code) {
                    dbQueries.updateStatus(SyncStatus.DELETED.code, path)
                }
            }
        }
    }
}

fun String?.cleanToNullIfEmpty(): String? {
    if (this.isNullOrEmpty())
        return null
    return this
}