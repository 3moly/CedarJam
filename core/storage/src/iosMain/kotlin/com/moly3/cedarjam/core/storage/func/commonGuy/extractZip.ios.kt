package com.moly3.cedarjam.core.storage.func.commonGuy

import com.moly3.cedarjam.core.storage.func.setLastWriteTimeUtc
import com.oldguy.common.io.ByteBuffer
import com.oldguy.common.io.File
import com.oldguy.common.io.FileMode
import com.oldguy.common.io.RawFile
import com.oldguy.common.io.ZipFile
import com.oldguy.common.io.use
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileItem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

actual suspend fun extractZip(
    archivePath: String,
    workspaceFullPath: String,
    serverFiles: List<FileItem>
)  {
    ZipFile(File(archivePath), mode = FileMode.Read).use { zip ->
        for (entry in zip.entries) {
            val absolutePath = pathWrapper(workspaceFullPath, entry.name).pathString
            val serverNode = serverFiles.firstOrNull { x -> x.relativePath == entry.name }
            if(entry.directory.name.contains("/")){
                val directoryPath = pathWrapper(workspaceFullPath, entry.directory.name.replaceAfterLast("/","")).pathString
                SystemFileSystem.createDirectories(Path(directoryPath))
            }

            if (serverNode != null) {
                SystemFileSystem.delete(Path(absolutePath), mustExist = false)
                try {
                    RawFile(File(absolutePath), FileMode.Write).use { file ->
                        zip.readEntry(entry) { entry, content, count, last ->
                            file.write(ByteBuffer(bytes = content.copyOf(count.toInt())))
                        }
                    }
                }catch (exc: Exception){}
                setLastWriteTimeUtc(absolutePath, modifiedTime = serverNode.modifiedTime)
            }
        }
    }
}