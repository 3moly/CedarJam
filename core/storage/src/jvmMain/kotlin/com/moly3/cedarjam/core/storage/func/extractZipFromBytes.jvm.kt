package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.FileStructure
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

actual fun extractZipFromBytes(
    bytes: ByteArray,
    destinationPath: String,
    fileStructure: FileStructure
) {
    val destDir = File(destinationPath)

    // Ensure destination directory exists
    if (!destDir.exists()) {
        destDir.mkdirs()
    }

    ByteArrayInputStream(bytes).use { byteStream ->
        ZipInputStream(byteStream).use { zipStream ->
            var entry = zipStream.nextEntry

            while (entry != null) {
                if (entry.name.contains(".db-shm") ||
                    entry.name.contains(".db-wal")
                ) {

                } else {
                    val filePath = File(destDir, entry.name)

                    if (entry.isDirectory) {
                        // Create directory if it doesn't exist
                        filePath.mkdirs()
                    } else {
                        // Ensure parent directory exists
                        filePath.parentFile?.mkdirs()

                        // Extract file and overwrite if exists
                        filePath.outputStream().use { output ->
                            zipStream.copyTo(output)
                        }
                    }
                    val foundMeta =
                        fileStructure.files.firstOrNull { d -> d.relativePath == entry.name }!!
                    setLastWriteTimeUtc(filePath.absolutePath, foundMeta.modifiedTime)
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }
    }
}