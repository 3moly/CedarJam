package com.moly3.cedarjam.core.storage.func

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

actual fun zipFolder(source: String, archive: String, relativePathsToSkip: List<String>) {
    val sourceFolder = File(source)
    val zipFile = File(archive)
    ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
        sourceFolder.walkTopDown().forEach { file ->
            val entryName = file.relativeTo(sourceFolder).path.replace(File.separatorChar, '/')

            if (file.isDirectory) {
                if (entryName.isNotEmpty()) { // avoid root folder itself
                    zos.putNextEntry(ZipEntry("$entryName/"))
                    zos.closeEntry()
                }
            } else {
                zos.putNextEntry(ZipEntry(entryName))
                FileInputStream(file).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }
}

actual suspend fun zipFolder(
    workspaceFolderAbsolutePath: String,
    filesToArchive: List<String>,
    archive: String
) {
    val sourceFolder = File(workspaceFolderAbsolutePath)
    val zipFile = File(archive)
    ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
        for(absolutePath in filesToArchive){
            val file = File(absolutePath)
            val entryName = file.relativeTo(sourceFolder).path.replace(File.separatorChar, '/')
            if (file.isDirectory) {
                if (entryName.isNotEmpty()) { // avoid root folder itself
                    zos.putNextEntry(ZipEntry("$entryName/"))
                    zos.closeEntry()
                }
            } else {
                zos.putNextEntry(ZipEntry(entryName))
                FileInputStream(file).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }
}