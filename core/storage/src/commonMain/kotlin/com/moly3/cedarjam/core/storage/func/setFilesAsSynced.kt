package com.moly3.cedarjam.core.storage.func


import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.indexdb.IndexDatabase
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun setFilesAsSynced(
    paths: List<String>,
    serverFiles: List<FileItem>,
    dbHelper: IndexDatabase,
    filesManager: ISystemFilesManager
) {
    // Превращаем список серверных файлов в Map для быстрого поиска хеша по пути
    // Нормализуем пути, чтобы не зависеть от слешей
    val serverFilesMap = serverFiles.associateBy { it.relativePath.normalizeText() }

    dbHelper.transaction {
        paths.forEach { relativePathStr ->
            val normalizedPath = relativePathStr.normalizeText()
            val fullPath = filesManager.toAbsoluteAppPath(pathWrapper(relativePathStr)).pathString

            // 1. Получаем реальные метаданные с диска
            val metadata = SystemFileSystem.metadataOrNull(Path(fullPath))
            val other = getOtherFileMeta(fullPath)
            if (metadata != null && metadata.isRegularFile) {

                val realModifiedTime = other.modifiedDateTime.toEpochMilliseconds() ?: 0L
                val realSize = metadata.size ?: 0L

                // 2. Ищем хеш, который прислал сервер
                // Если вдруг файла нет в списке (странно), то хеш пустой,
                // но лучше так, чем краш. В идеале можно пересчитать.
                val serverFile = serverFilesMap[normalizedPath]
                val hash = serverFile?.contentHash ?: ""

                // 3. Пишем в базу
                dbHelper.indexFileQueries.upsertSyncedFile(
                    relativePath = relativePathStr,
                    contentHash = hash,
                    modifiedTime = realModifiedTime,
                    size = realSize,
                    isDirectory = 0L, // Мы скачиваем только файлы
                    // lastSyncedHash и status проставятся в SQL запросе
                )
            }
        }
    }
}