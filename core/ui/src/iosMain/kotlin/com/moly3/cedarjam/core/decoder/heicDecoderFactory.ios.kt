package com.moly3.cedarjam.core.decoder

import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextGetData
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRectMake
import platform.ImageIO.CGImageSourceCreateImageAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.posix.memcpy

/*
 * iosMain
 *
 * iOS has first-class HEIC support since iOS 11 — it is in fact the default
 * camera format. The system `ImageIO` framework decodes it via
 * `CGImageSource`. Coil on iOS renders through Skiko, so the job here is:
 *
 *   1. Read the HEIC bytes from Coil's ImageSource.
 *   2. Decode to a CGImage with ImageIO.
 *   3. Draw the CGImage into a CoreGraphics RGBA8888 bitmap context.
 *   4. Copy those raw pixels straight into a Skia Bitmap.
 *
 * Step 3 is necessary because there is no direct CGImage -> Skia bridge; we go
 * through a known-layout pixel buffer (premultiplied RGBA, 8 bits each).
 *
 * Note: unlike the original draft, the decoder produces a Skia *Bitmap* and
 * returns it directly via `asImage()`. There is no SkiaImage -> readPixels
 * round-trip — that step was redundant work and a common source of
 * `readPixels` returning false.
 */

/**
 * iOS HEIC decoder backed by ImageIO / `CGImageSource`.
 */
class IosHeicDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult = withContext(Dispatchers.Default) {
        val bytes = source.source().buffer().readByteArray()
        val bitmap = decodeHeicToSkiaBitmap(bytes)
            ?: error("ImageIO failed to decode HEIC image")

        DecodeResult(
            image = bitmap.asImage(),
            isSampled = false,
        )
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            if (!isHeic(result.source)) return null
            return IosHeicDecoder(result.source, options)
        }
    }
}

/** iOS `actual`. ImageIO HEIC support is always present on supported iOS versions. */
actual fun heicDecoderFactory(): Decoder.Factory? = IosHeicDecoder.Factory()

/**
 * Decodes [heicBytes] using ImageIO and returns the result as a Skia [Bitmap],
 * or `null` if ImageIO cannot decode the data.
 */
@OptIn(ExperimentalForeignApi::class)
private fun decodeHeicToSkiaBitmap(heicBytes: ByteArray): Bitmap? {
    if (heicBytes.isEmpty()) return null

    // --- 1. ByteArray -> CFData --------------------------------------------
    val cfData = heicBytes.usePinned { pinned ->
        CFDataCreate(
            allocator = null,
            bytes = pinned.addressOf(0).reinterpret(),
            length = heicBytes.size.toLong(),
        )
    } ?: return null

    try {
        // --- 2. CFData -> CGImage via ImageIO ------------------------------
        val imageSource = CGImageSourceCreateWithData(cfData, null) ?: return null
        try {
            val cgImage: CGImageRef =
                CGImageSourceCreateImageAtIndex(imageSource, 0u, null) ?: return null
            try {
                return cgImageToSkiaBitmap(cgImage)
            } finally {
                CFRelease(cgImage)
            }
        } finally {
            CFRelease(imageSource)
        }
    } finally {
        CFRelease(cfData)
    }
}

/**
 * Draws [cgImage] into an RGBA8888 premultiplied CoreGraphics context and
 * copies the pixels into an immutable Skia [Bitmap].
 */
@OptIn(ExperimentalForeignApi::class)
private fun cgImageToSkiaBitmap(cgImage: CGImageRef): Bitmap? {
    val width = CGImageGetWidth(cgImage).toInt()
    val height = CGImageGetHeight(cgImage).toInt()
    if (width <= 0 || height <= 0) return null

    val bytesPerPixel = 4
    val rowBytes = width * bytesPerPixel
    val byteCount = rowBytes * height

    val colorSpace = CGColorSpaceCreateDeviceRGB() ?: return null
    try {
        // Premultiplied RGBA — matches Skia's RGBA_8888 / PREMUL.
        val context = CGBitmapContextCreate(
            data = null, // let CoreGraphics allocate the backing buffer
            width = width.toULong(),
            height = height.toULong(),
            bitsPerComponent = 8u,
            bytesPerRow = rowBytes.toULong(),
            space = colorSpace,
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
        ) ?: return null

        try {
            // Draw the decoded image into the context.
            CGContextDrawImage(
                context,
                CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()),
                cgImage,
            )

            val pixels = CGBitmapContextGetData(context) ?: return null

            // Copy CoreGraphics pixels into a Kotlin ByteArray.
            val buffer = ByteArray(byteCount)
            buffer.usePinned { pinned ->
                memcpy(pinned.addressOf(0), pixels, byteCount.toULong())
            }

            // --- Build a Skia Bitmap from the raw pixels -------------------
            val imageInfo = ImageInfo(
                width = width,
                height = height,
                colorType = ColorType.RGBA_8888,
                alphaType = ColorAlphaType.PREMUL,
            )
            return Bitmap().apply {
                allocPixels(imageInfo)
                installPixels(buffer)
                setImmutable()
            }
        } finally {
            CGContextRelease(context)
        }
    } finally {
        CGColorSpaceRelease(colorSpace)
    }
}