package com.moly3.cedarjam.core.decoder

import android.graphics.BitmapFactory
import android.os.Build
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlinx.coroutines.runInterruptible

/**
 * Android HEIC decoder.
 *
 * Android's platform `BitmapFactory` already decodes HEIC on **API 28+**
 * (Android 9 Pie), so Coil's stock `BitmapFactoryDecoder` will usually handle
 * HEIC for you with no extra code.
 *
 * This explicit decoder still earns its place when you want:
 *  - a single, predictable HEIC path that matches the JVM/iOS decoders;
 *  - a clean point to plug in a third-party codec (e.g. awxkee/avif-coder) for
 *    API < 28, HDR, or to dodge specific OEM decoding bugs.
 *
 * As written it simply uses the platform decoder; replace the body of
 * [decode] with a library call if you need API < 28 support.
 */
class AndroidHeicDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {

    override suspend fun decode(): DecodeResult = runInterruptible {
        val opts = BitmapFactory.Options().apply {
            // Honour Coil's requested size for memory efficiency.
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }

        val bitmap = source.source().inputStream().use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
        } ?: error("Android BitmapFactory failed to decode HEIC")

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
            // Platform HEIC support starts at API 28. Below that, decline and
            // let another factory (or a bundled library) try.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
            if (!isHeic(result.source)) return null
            return AndroidHeicDecoder(result.source, options)
        }
    }
}

/**
 * Android `actual`. Returns `null` on API < 28 where the platform cannot
 * decode HEIC — callers should fall back to the stock decoder or a library.
 */
actual fun heicDecoderFactory(): Decoder.Factory? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        AndroidHeicDecoder.Factory()
    } else {
        null
    }
}