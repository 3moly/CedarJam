package com.moly3.cedarjam.core.storage.func

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileStructure
import kotlinx.cinterop.*
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun extractZipFromBytes(
    bytes: ByteArray,
    destinationPath: String,
    fileStructure: FileStructure
) {
    val fileManager = NSFileManager.defaultManager

    // Ensure destination directory exists
    if (!fileManager.fileExistsAtPath(destinationPath)) {
        fileManager.createDirectoryAtPath(
            path = destinationPath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }

    // Create NSData from ByteArray
    val data = bytes.usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = bytes.size.toULong()
        )
    }

    // Create temporary zip file
    val tempDir = NSTemporaryDirectory()
    val tempZipPath = "$tempDir${NSUUID.UUID().UUIDString}.zip"

    // Write to temporary file
    val writeSuccess = data.writeToFile(tempZipPath, atomically = true)

    if (!writeSuccess) {
        Logger.w("Failed to write temp zip file")
        return
    }

    // Extract using SSZipArchive
//   todo val success = SSZipArchive.unzipFileAtPath(
//        path = tempZipPath,
//        toDestination = destinationPath,
//        overwrite = true,
//        password = null,
//        error = null
//    )
//
//    if (success) {
//        Logger.w("Zip extracted successfully to: $destinationPath")
//        for (item in fileStructure.files) {
//            setLastWriteTimeUtc(
//                pathWrapper(destinationPath, item.relativePath).pathString,
//                modifiedTime = item.modifiedTime
//            )
//        }
//    } else {
//        Logger.w("Failed to extract zip")
//    }
//
//    // Clean up temporary file
//    fileManager.removeItemAtPath(tempZipPath, error = null)
}