package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.create
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun ImageBitmap.saveAsPng(path: String) {
    withContext(Dispatchers.Default) {

        val skiaBitmap = this@saveAsPng.asSkiaBitmap()

        // Encode to PNG bytes
        val skiaImage = Image.makeFromBitmap(skiaBitmap)
        val dataBytes = skiaImage.encodeToData(EncodedImageFormat.PNG)
            ?: error("Failed to encode PNG")

        val nsData = dataBytes.bytes.usePinned {
            NSData.create(bytes = it.addressOf(0), length = dataBytes.bytes.size.toULong())
        }

        val fileManager = NSFileManager.defaultManager

        // Ensure directory exists
        val parentPath = path.substringBeforeLast("/", "")
        if (parentPath.isNotEmpty() &&
            !fileManager.fileExistsAtPath(parentPath)
        ) {
            fileManager.createDirectoryAtPath(
                parentPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }

        val success = nsData.writeToFile(path, atomically = true)
        if (!success) {
            error("Failed to write PNG file")
        }
    }
}