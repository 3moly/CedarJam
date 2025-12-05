package com.moly3.cedarjam.core.storage.func.commonGuy

import com.oldguy.common.io.FileMode
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.oldguy.common.io.File
import com.oldguy.common.io.ZipFile

//actual suspend fun packToZip(
//    workspaceFolderAbsolutePath: String,
//    filesToArchive: List<String>,
//    archivePath: String
//) {
//    ZipFile(
//        File(archivePath),
//        mode = FileMode.Write,
//        zip64 = false
//    ).use {
//        for (absolutePath in filesToArchive) {
//            val inputFile = File(pathWrapper(workspaceFolderAbsolutePath, absolutePath).pathString)
//            it.zipFile(
//                inputFile,
//                entryName = absolutePath
//            )
//        }
//    }
//}