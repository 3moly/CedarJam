package com.moly3.cedarjam.core.decoder

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlinx.coroutines.runInterruptible
import okio.buffer
import org.jetbrains.skia.Image as SkiaImage
import java.io.File

/**
 * JVM/Desktop HEIC decoder.
 *
 * Pipeline:
 *  1. Spill the HEIC bytes from Coil's [ImageSource] to a temp file
 *     (the external codecs all want a real path).
 *  2. Invoke an external codec ([JvmHeicCodec]) to transcode HEIC -> PNG.
 *  3. Hand the PNG bytes to Skia's `Image.makeFromEncoded`, which Compose
 *     Desktop can render directly.
 *
 * The blocking subprocess + file I/O is wrapped in [runInterruptible] so that
 * cancelling the Coil request also kills the work.
 */
class JvmHeicDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val codec: JvmHeicCodec,
) : Decoder {

    override suspend fun decode(): DecodeResult = runInterruptible {
        val input = File.createTempFile("coil-heic-in-", ".heic")
        val output = File.createTempFile("coil-heic-out-", ".png")
        try {
            // 1. Materialize input.
            input.outputStream().use { out ->
                source.source().buffer().inputStream().copyTo(out)
            }

            // 2. Transcode.
            codec.convertToPng(input, output)
            check(output.length() > 0) { "HEIC codec produced an empty file" }

            // 3. Decode the PNG with Skia.
            val pngBytes = output.readBytes()
            val skiaImage = SkiaImage.makeFromEncoded(pngBytes)
//            val pngBytes = output.readBytes()

// Decode PNG -> Skia Image, then rasterize into a Skia Bitmap.
//            val skiaImage = SkiaImage.makeFromEncoded(pngBytes)
            val bitmap = coil3.Bitmap().apply {
                allocPixels(skiaImage.imageInfo)
            }
            val ok = skiaImage.readPixels(bitmap, 0, 0)
            check(ok) { "Failed to read HEIC pixels into Skia Bitmap" }
            bitmap.setImmutable()

            DecodeResult(
                image = bitmap.asImage(),   // coil3.asImage on org.jetbrains.skia.Bitmap
                isSampled = false,
            )
        } finally {
            input.delete()
            output.delete()
        }
    }

    /**
     * @param explicitBinary optional absolute path to a HEIC codec binary you
     *        bundle with your app. When null, the codec is auto-detected from
     *        PATH and well-known install locations.
     */
    class Factory(
        private val explicitBinary: String? = null,
    ) : Decoder.Factory {

        // Detect once; codec lookup touches the filesystem.
        private val codec: JvmHeicCodec? by lazy {
            JvmHeicCodec.detect(explicitBinary)
        }

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            val codec = codec ?: return null            // no codec available
            if (!isHeic(result.source)) return null     // not a HEIC file
            return JvmHeicDecoder(result.source, options, codec)
        }
    }
}

/**
 * JVM `actual`. Returns `null` if no HEIC codec can be found on this machine,
 * so registration via `heicDecoderFactory()?.let { add(it) }` degrades safely.
 */
actual fun heicDecoderFactory(): Decoder.Factory? {
    // Probe for a codec up front; if none exists, don't register the factory.
    return if (JvmHeicCodec.detect() != null) JvmHeicDecoder.Factory() else null
}

/**
 * Variant for apps that bundle their own codec binary (recommended for
 * distribution, so you don't depend on what the user happens to have installed).
 *
 * Point [binaryPath] at the binary inside your packaged app resources.
 */
fun heicDecoderFactory(binaryPath: String): Decoder.Factory? {
    return if (JvmHeicCodec.detect(binaryPath) != null) {
        JvmHeicDecoder.Factory(binaryPath)
    } else {
        null
    }
}