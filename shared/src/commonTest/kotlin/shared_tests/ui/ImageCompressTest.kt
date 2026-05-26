package shared_tests.ui

import com.moly3.cedarjam.core.domain.func.formatFileSize
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.moly3.cedarjam.shared.func.convertToWebp
import kotlinx.coroutines.test.runTest
import shared_tests.base.getTestResourcePath
import kotlin.test.Test

class ImageCompressTest {

    @Test
    fun test_webp() = runTest {
        val images = listOf(
            "images/jpeg.jpeg",
            "images/avif.avif",
            "images/heif.heif",
            "images/jpg.jpg",
            "images/jpeg.jpeg",
            "images/webp.webp",
            "images/png.png",
        )
        val fs = createSystemFilesManager()

        for(image in images){
            val imgPath = getTestResourcePath(image)
            val bytes = fs.getNodeBytes(imgPath)
            println("Original size: ${formatFileSize(bytes.size.toLong())}")

            // Extract base name and extension so we can accurately save the original
            val baseName = image.substringAfterLast("/").substringBeforeLast(".")
            val extension = image.substringAfterLast(".")

            // --- NEW: Save the original file first ---
            val originalOutputPath = "build/test-outputs/webp-conversions/${baseName}_original.$extension"
            fs.setNodeBytes(originalOutputPath, bytes)
            println("Saved original to: $originalOutputPath")

            val qualitySteps = listOf(100, 90, 80, 70, 60, 50, 40, 30, 20, 10)

            for (quality in qualitySteps) {
                val byteArrayWebp = convertToWebp(bytes, quality)
                if (byteArrayWebp != null) {
                    println("Quality $quality: ${formatFileSize(byteArrayWebp.size.toLong())}")

                    val outputPath = "build/test-outputs/webp-conversions/${baseName}_q${quality}.webp"
                    fs.setNodeBytes(
                        outputPath,
                        byteArray = byteArrayWebp
                    )
                } else {
                    println("Quality $quality: conversion returned null")
                }
            }

            // Just adding a visual separator in the logs for readability between different images
            println("--------------------------------------------------")
        }
    }
}