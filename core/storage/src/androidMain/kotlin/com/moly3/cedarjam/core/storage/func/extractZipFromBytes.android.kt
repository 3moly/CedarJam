package com.moly3.cedarjam.core.storage.func

import android.os.Build
import androidx.annotation.RequiresApi
import com.moly3.cedarjam.core.domain.model.FileStructure
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

@RequiresApi(Build.VERSION_CODES.O)
actual fun extractZipFromBytes(
    bytes: ByteArray,
    destinationPath: String,
    fileStructure: FileStructure
) {
    val destDir = File(destinationPath)
    if (!destDir.exists()) {
        destDir.mkdirs()
    }
    ByteArrayInputStream(bytes).use { byteStream ->
        ZipInputStream(byteStream).use { zipStream ->
            var entry = zipStream.nextEntry

            while (entry != null) {
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

                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }
        }
    }
}