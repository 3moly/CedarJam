package com.moly3.cedarjam.shared.func

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun convertToWebp(inputBytes: ByteArray, quality: Int): ByteArray? {
    return try {
        // 1. Decode the raw JPG/PNG bytes into a Skia Image
        val image = Image.makeFromEncoded(inputBytes)

        // 2. Encode the image directly to WebP data
        val webpData = image.encodeToData(EncodedImageFormat.WEBP, quality)

        // 3. Extract the Kotlin ByteArray from the Skia Data wrapper
        webpData?.bytes
    } catch (e: Exception) {
        println("Failed to convert image: ${e.message}")
        null
    }
}