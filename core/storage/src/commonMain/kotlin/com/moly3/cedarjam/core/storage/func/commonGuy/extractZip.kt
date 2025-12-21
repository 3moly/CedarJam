package com.moly3.cedarjam.core.storage.func.commonGuy

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.storage.func.setLastWriteTimeUtc
import com.oldguy.common.io.ByteBuffer
import com.oldguy.common.io.File
import com.oldguy.common.io.FileMode
import com.oldguy.common.io.RawFile
import com.oldguy.common.io.ZipFile
import com.oldguy.common.io.use
import kotlinx.coroutines.delay
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend fun extractZip(
    archivePath: String,
    workspaceFullPath: String,
): List<String> {
    val extractedFiles = mutableListOf<String>()
    ZipFile(File(archivePath), mode = FileMode.Read).use { zip ->
        for (entry in zip.entries) {
            val absolutePath = pathWrapper(workspaceFullPath, entry.name).pathString

            if (entry.comment == "directory") {
                SystemFileSystem.createDirectories(Path(absolutePath))
                extractedFiles.add(entry.name)
            } else {
                if (entry.directory.name.contains("/")) {
                    val directoryPath = pathWrapper(
                        workspaceFullPath,
                        entry.directory.name.replaceAfterLast("/", "")
                    ).pathString
                    SystemFileSystem.createDirectories(Path(directoryPath))
                }
                try {
                    SystemFileSystem.delete(Path(absolutePath), mustExist = true)
                } catch (exc: Exception) {
                    val erer = exc.message + ""
                }
                try {
                    RawFile(File(absolutePath), FileMode.Write).use { file ->
                        zip.readEntry(entry) { entry, content, count, last ->
                            file.write(ByteBuffer(bytes = content.copyOf(count.toInt())))
                        }
                    }
                    delay(100L)
                    val modifiedTime = entry.comment.toLong()
                    setLastWriteTimeUtc(absolutePath, modifiedTime = modifiedTime)
                    Logger.e { "1224 extractZip: success ${entry.name}" }
                    extractedFiles.add(entry.name)
                } catch (exc: Exception) {
                    Logger.e { "1224 extractZip: error ${exc.message}" }
                    val msg = "" + exc.message
                }
            }
        }
    }
    return extractedFiles
}