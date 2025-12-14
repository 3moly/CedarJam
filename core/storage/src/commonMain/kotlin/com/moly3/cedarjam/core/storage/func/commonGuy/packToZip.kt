package com.moly3.cedarjam.core.storage.func.commonGuy

import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.oldguy.common.io.File
import com.oldguy.common.io.FileMode
import com.oldguy.common.io.ZipFile

suspend fun packToZip(
    workspaceFolderAbsolutePath: String,
    filesToArchive: List<String>,
    archivePath: String,
) {
    ZipFile(
        File(archivePath),
        mode = FileMode.Write,
        zip64 = false
    ).use {
        for (absolutePath in filesToArchive) {
            val inputFile = File(pathWrapper(workspaceFolderAbsolutePath, absolutePath).pathString)
            if (inputFile.isDirectory) {
                it.zipDirectory(inputFile, shallow = true, filter = {false})
            } else {
                it.zipFile(
                    inputFile,
                    entryName = absolutePath
                )
            }
        }
    }
}