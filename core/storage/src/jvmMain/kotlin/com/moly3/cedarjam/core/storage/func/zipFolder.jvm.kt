package com.moly3.cedarjam.core.storage.func

import com.oldguy.common.io.ZipFile
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTreeNode
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
            if (entryName.contains(".DS_Store") || entryName.contains("__MACOSX"))
                return@forEach

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

suspend fun ZipFile.archiving(
    fileNode: FileTreeNode,
    filesToArchive: List<String>,
) {
    val absolutePath = fileNode.getFullPath()

    when (fileNode) {
        is FileTreeNode.Directory -> {
            val needToCheck = filesToArchive.firstOrNull { x -> x.contains(absolutePath) }
            if (needToCheck != null) {
                for (node in fileNode.getChildrenOrNull() ?: listOf()) {
                    this.archiving(
                        fileNode = node,
                        filesToArchive = filesToArchive
                    )
                }
            }
        }

        is FileTreeNode.File -> {
            val found = filesToArchive.firstOrNull { x -> x == absolutePath }
            if (found != null) {
                this.zipFile(com.oldguy.common.io.File(absolutePath))
            }
        }
    }
}

actual suspend fun zipFolder(
    workspaceFolderAbsolutePath: String,
    filesToArchive: List<String>,
    archive: String
) {
//    val sourceFolder = File(workspaceFolderAbsolutePath)
//    com.oldguy.common.io.ZipFile(
//        com.oldguy.common.io.File(archive),
//        mode = FileMode.Write,
//        zip64 = false
//    ).use {
//        for (absolutePath in filesToArchive) {
//            val file = File(absolutePath)
//            val entryName = file.relativeTo(sourceFolder).path.replace(File.separatorChar, '/')
//            it.zipFile(com.oldguy.common.io.File(absolutePath), entryName = entryName)
//        }
//    }
    val sourceFolder = File(workspaceFolderAbsolutePath)
    val zipFile = File(archive)
    ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
        for(absolutePath in filesToArchive){
            val file = File(pathWrapper(workspaceFolderAbsolutePath,absolutePath).pathString)
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