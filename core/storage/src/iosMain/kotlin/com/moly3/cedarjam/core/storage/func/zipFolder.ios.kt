package com.moly3.cedarjam.core.storage.func

import co.touchlab.kermit.Logger
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi


@OptIn(ExperimentalForeignApi::class)
actual fun zipFolder(source: String, archive: String, relativePathsToSkip: List<String>) {
    val fileManager = NSFileManager.defaultManager
    val sourcePath = source
    val archivePath = archive
    Logger.w{"success maked archive 1"}
    // Ensure the archive directory exists
    val archiveDir = (archivePath as NSString).stringByDeletingLastPathComponent
    if (!fileManager.fileExistsAtPath(archiveDir)) {
        fileManager.createDirectoryAtPath(
            archiveDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }
    Logger.w{"success maked archive 2"}
    // Get all files and directories to zip
    val enumerator = fileManager.enumeratorAtPath(sourcePath)
    val filesToZip = mutableListOf<String>()
    Logger.w{"success maked archive 3"}
    while (true) {
        Logger.w{"success maked archive 4"}
        val relativePath = enumerator?.nextObject() as? String ?: break

        // Skip .DS_Store and __MACOSX
        if (relativePath.contains(".DS_Store") || relativePath.contains("__MACOSX")) {
            continue
        }

        // Skip paths in relativePathsToSkip
        if (relativePathsToSkip.any { relativePath.startsWith(it) }) {
            continue
        }

        val fullPath = (sourcePath as NSString).stringByAppendingPathComponent(relativePath)
        filesToZip.add(fullPath)

    }
    Logger.w{"success maked archive 5. filesToZip: ${filesToZip.size}"}
//    // Create zip archive using SSZipArchive
//    val success = SSZipArchive.createZipFileAtPath(
//        path = archive,
//        withFilesAtPaths = filesToZip,
//        withPassword = null,
////        progressHandler = { entryNumber, total ->
////            // Progress callback helps prevent apparent freezing
////            entryNumber.toInt()
////            Logger.w("Progress: $entryNumber/$total ${filesToZip[entryNumber.toInt()-1]}")
////            true // Return true to continue
////        }
//    )

//    val success = SSZipArchive.createZipFileAtPath(
//        path = archive,
//        withContentsOfDirectory = source,
//        keepParentDirectory = false,
//        withPassword = null,
//        andProgressHandler = { entryNumber, total ->
//            // Progress callback helps prevent apparent freezing
//            println("Progress: $entryNumber/$total")
//            true // Return true to continue
//        }
//    )
    Logger.w{"success maked archive 6"}
//    if (!success) {
//        throw Exception("Failed to create zip archive at: $archivePath")
//    }else{
//        Logger.w{"success maked archive"}
//    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun zipFolder(
    workspaceFolderAbsolutePath: String,
    filesToArchive: List<String>,
    archive: String
) {
//    val success = SSZipArchive.createZipFileAtPath(
//        path = archive,
//        withContentsOfDirectory = workspaceFolderAbsolutePath,
//        keepParentDirectory = false,
//        withPassword = null,
//        andProgressHandler = { entryNumber, total ->
//            // Progress callback helps prevent apparent freezing
//            println("Progress: $entryNumber/$total")
//            true // Return true to continue
//        }
//    )
//    Logger.w{"success maked archive 6"}
//    if (!success) {
//        throw Exception("Failed to create zip archive at: $archive")
//    }else{
//        Logger.w{"success maked archive"}
//    }
}