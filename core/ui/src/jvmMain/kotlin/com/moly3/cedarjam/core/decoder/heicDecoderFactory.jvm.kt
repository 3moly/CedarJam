package com.moly3.cedarjam.core.decoder

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import java.security.MessageDigest
import openize.heic.decoder.HeicImage
import openize.heic.decoder.HeicImageFrame
import openize.heic.decoder.PixelFormat
import openize.io.IOFileStream
import openize.io.IOMode

/*
 * jvmMain
 *
 * Desktop/JVM HEIC decoder backed by Openize.HEIC (pure-JVM, no native binary).
 *
 *   maven { url = uri("https://releases.aspose.com/java/repo/") }
 *   implementation("com.aspose:openize-heic:25.4")
 *
 * ─── PERFORMANCE ────────────────────────────────────────────────────────────
 * Openize.HEIC is a from-scratch Java HEVC decoder. It is SLOW — a large modern
 * iPhone HEIC (12-48 MP) takes multiple seconds to decode.
 *
 * Two layers keep that from hurting:
 *
 *   1. HEIC_DECODE_GATE — a process-wide semaphore capping concurrent decodes
 *      at 2, so parallel pure-Java decodes cannot pin every core and freeze
 *      the UI. The decode also runs on Dispatchers.IO.
 *
 *   2. PNG transcode cache — after the first successful decode, the result is
 *      written to disk as a PNG. Subsequent loads (including after an app
 *      restart) decode that PNG in milliseconds and Openize never runs again.
 *      This is necessary because Coil's own disk cache stores the *encoded*
 *      HEIC, so a Coil disk-cache hit would still re-run the slow Openize
 *      decode every cold start.
 *
 * The cache is opt-in: construct the factory with a cache directory. Without
 * one, the decoder still works — it just decodes with Openize every time
 * (Coil's in-memory cache still avoids re-decoding within a session).
 *
 * ─── STREAM LIFETIME ────────────────────────────────────────────────────────
 * HeicImage.load() parses container metadata only; pixels are decoded later
 * inside getInt32Array(). The backing IOFileStream must stay open until every
 * pixel read is done — it is opened once and closed only in finally.
 * ────────────────────────────────────────────────────────────────────────────
 */

/**
 * Process-wide cap on concurrent HEIC decodes. Pure-Java HEVC decoding is
 * CPU-bound; allowing only 2 at once leaves cores free for UI rendering.
 */
private val HEIC_DECODE_GATE = Semaphore(permits = 2)

/**
 * Desktop/JVM HEIC decoder backed by the pure-JVM Openize.HEIC library.
 *
 * @param cacheDir optional directory for the PNG transcode cache. When
 *        non-null, a decoded HEIC is written there as a PNG and reused on
 *        later loads (Openize runs at most once per image). When null,
 *        Openize decodes on every load.
 */
class JvmHeicDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val cacheDir: File?,
) : Decoder {

    override suspend fun decode(): DecodeResult =
        HEIC_DECODE_GATE.withPermit {
            runInterruptible(Dispatchers.IO) { decodeBlocking() }
        }

    private fun decodeBlocking(): DecodeResult {
        // 1. Resolve a complete file for Openize (its API is file-backed).
        val heicFile: File = okioPathToFile(source.file())
        val fileLen = heicFile.length()
        check(fileLen > 0) { "HEIC source file was empty (${heicFile.absolutePath})" }

        // 2. PNG cache fast path. If a transcoded PNG already exists, decode
        //    that and skip Openize entirely.
        val pngFile: File? = cachePngFileFor(heicFile, fileLen)
        if (pngFile != null && pngFile.isFile && pngFile.length() > 0) {
            decodeCachedPng(pngFile)?.let { return it }
            // Cache file present but unreadable/corrupt — drop it and fall
            // through to a fresh Openize decode.
            runCatching { pngFile.delete() }
        }

        // 3. Slow path: decode with Openize.
        val bitmap = decodeWithOpenize(heicFile, fileLen)

        // 4. Populate the cache for next time (best-effort; never fatal).
        if (pngFile != null) {
            runCatching { writePngAtomically(bitmap, pngFile) }
        }

        return DecodeResult(image = bitmap.asImage(), isSampled = false)
    }

    /** Decodes a previously cached PNG. Returns null if it can't be read. */
    private fun decodeCachedPng(pngFile: File): DecodeResult? = runCatching {
        val skiaImage = SkiaImage.makeFromEncoded(pngFile.readBytes())
        val bitmap = Bitmap().apply {
            allocPixels(skiaImage.imageInfo)
            check(skiaImage.readPixels(this, 0, 0)) { "readPixels failed on cached PNG" }
            setImmutable()
        }
        DecodeResult(image = bitmap.asImage(), isSampled = false)
    }.getOrNull()

    /** Full Openize decode of [heicFile] into a Skia [Bitmap]. */
    private fun decodeWithOpenize(heicFile: File, fileLen: Long): Bitmap {
        // The stream must stay open through getInt32Array() — close in finally.
        val fs = IOFileStream(heicFile.absolutePath, IOMode.READ)
        try {
            val image: HeicImage = try {
                HeicImage.load(fs)
            } catch (e: IndexOutOfBoundsException) {
                // Openize's box parser ran past EOF on a known-complete file —
                // this Openize version cannot parse this file (often iOS18+
                // containers). Bumping the openize-heic dependency is the fix.
                error(
                    "Openize.HEIC crashed parsing the HEIC container " +
                            "(file is $fileLen bytes, not truncated). Try a newer " +
                            "openize-heic release. Cause: $e",
                )
            } ?: error("Openize.HEIC could not parse the HEIC container")

            val frame: HeicImageFrame = selectImageFrame(image)

            val width = frame.width.toInt()
            val height = frame.height.toInt()
            check(width > 0 && height > 0) {
                "Openize.HEIC reported invalid frame dimensions: ${width}x$height"
            }

            val argb: IntArray = frame.getInt32Array(PixelFormat.Argb32)
                ?: error(
                    "Openize.HEIC returned no pixels for the selected frame " +
                            "(id=${frame.id}, ${width}x$height). " + describeFrames(image),
                )
            check(argb.size >= width * height) {
                "Openize.HEIC pixel buffer too small: ${argb.size} < ${width * height}"
            }

            return Bitmap().apply {
                allocPixels(
                    ImageInfo(
                        width = width,
                        height = height,
                        colorType = ColorType.RGBA_8888,
                        alphaType = ColorAlphaType.UNPREMUL,
                    ),
                )
                installPixels(argbIntsToRgbaBytes(argb, width, height))
                setImmutable()
            }
        } finally {
            runCatching { fs.close() }
        }
    }

    /**
     * Resolves the PNG cache file for this image, or null if caching is off.
     *
     * The cache key prefers Coil's own `options.diskCacheKey` (already a
     * stable per-image key). When that is absent, it falls back to a SHA-256
     * of the HEIC file's path + length + last-modified time — cheap, and
     * stable for a given on-disk file.
     */
    private fun cachePngFileFor(heicFile: File, fileLen: Long): File? {
        val dir = cacheDir ?: return null
        runCatching { dir.mkdirs() }
        val rawKey = options.diskCacheKey
            ?: "${heicFile.absolutePath}|$fileLen|${heicFile.lastModified()}"
        return File(dir, sha256Hex(rawKey) + ".png")
    }

    /**
     * Encodes [bitmap] to PNG and writes it to [target] atomically: write to a
     * sibling temp file, then rename. A crash or cancellation mid-write can
     * therefore never leave a corrupt PNG that would poison the cache.
     */
    private fun writePngAtomically(bitmap: Bitmap, target: File) {
        val data = SkiaImage.makeFromBitmap(bitmap)
            .encodeToData(EncodedImageFormat.PNG)
            ?: error("Skia failed to encode the HEIC bitmap to PNG")

        val tmp = File.createTempFile("heic-png-", ".tmp", target.parentFile)
        try {
            tmp.writeBytes(data.bytes)
            // Atomic on the same filesystem; fall back to copy+delete if not.
            if (!tmp.renameTo(target)) {
                tmp.copyTo(target, overwrite = true)
            }
        } finally {
            runCatching { tmp.delete() }
        }
    }

    /**
     * @param cacheDir directory for the PNG transcode cache, or null to
     *        disable caching (Openize then decodes on every load).
     */
    class Factory(
        private val cacheDir: File? = null,
    ) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            if (!isHeic(result.source)) return null
            return JvmHeicDecoder(result.source, options, cacheDir)
        }
    }
}

/**
 * JVM `actual` — no caching. Openize.HEIC is always present once the artifact
 * is on the classpath, so the factory is always returned.
 *
 * To enable the PNG transcode cache, use the [heicDecoderFactory] overload
 * that takes a cache directory.
 */
actual fun heicDecoderFactory(): Decoder.Factory? = JvmHeicDecoder.Factory(appCacheDir())

/**
 * JVM HEIC decoder factory WITH a PNG transcode cache.
 *
 * Pass a persistent directory (your app's cache dir — NOT the OS temp dir,
 * which is wiped on reboot and would discard the cache every cold start).
 * After the first decode of an image, Openize is never run for it again.
 */
fun heicDecoderFactory(cacheDir: File): Decoder.Factory =
    JvmHeicDecoder.Factory(cacheDir)

fun appCacheDir(): File {
    val os = System.getProperty("os.name").lowercase()
    val home = System.getProperty("user.home")
    val base = when {
        os.contains("mac") -> File(home, "Library/Caches")
        os.contains("win") -> File(System.getenv("LOCALAPPDATA") ?: "$home\\AppData\\Local")
        else -> File(System.getenv("XDG_CACHE_HOME") ?: "$home/.cache")
    }
    return File(base, "cedarjam/heic-cache").apply { mkdirs() }
}

/**
 * Picks the frame to render from a parsed [HeicImage].
 *
 * Preference order:
 *   1. The default frame, if it is a real, non-hidden image frame.
 *   2. Otherwise the first non-hidden, non-derived image frame.
 *   3. Otherwise the first image frame at all.
 *
 * "Derived" frames (rotation/overlay transforms) and auxiliary frames
 * (depth maps, alpha) carry no standalone pixel data, which is why the
 * default frame alone is not trustworthy.
 */
private fun selectImageFrame(image: HeicImage): HeicImageFrame {
    val default: HeicImageFrame? = runCatching { image.defaultFrame }.getOrNull()
    if (default != null && default.isImage && !default.isHidden) return default

    val frames = runCatching { image.frames }.getOrNull()?.values.orEmpty()

    frames.firstOrNull { it.isImage && !it.isHidden && !it.isDerived }
        ?.let { return it }
    frames.firstOrNull { it.isImage && !it.isHidden }
        ?.let { return it }
    frames.firstOrNull { it.isImage }
        ?.let { return it }

    error("Openize.HEIC found no decodable image frame. " + describeFrames(image))
}

/** SHA-256 of [input], lowercase hex — used for stable cache filenames. */
private fun sha256Hex(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.encodeToByteArray())
    return digest.joinToString("") { b -> ((b.toInt() and 0xFF) + 0x100).toString(16).substring(1) }
}

/**
 * Converts the [okio.Path] returned by `ImageSource.file()` to a [java.io.File].
 *
 * (Okio also ships a `Path.toFile()` JVM extension — if your Okio version
 * exposes it, `source.file().toFile()` is equivalent and you can inline this.)
 */
private fun okioPathToFile(path: okio.Path): File = File(path.toString())

/**
 * Builds a short human-readable inventory of every frame Openize parsed.
 * Used only in error messages, to make an undecodable file diagnosable.
 */
private fun describeFrames(image: HeicImage): String {
    val frames = runCatching { image.frames }.getOrNull()?.values.orEmpty()
    if (frames.isEmpty()) return "No frames were parsed."
    val rows = frames.joinToString("; ") { f ->
        runCatching {
            "id=${f.id} type=${f.imageType} ${f.width}x${f.height} " +
                    "image=${f.isImage} hidden=${f.isHidden} derived=${f.isDerived}"
        }.getOrElse { "id=? (frame inspection failed)" }
    }
    return "Frames parsed: $rows"
}

/**
 * Converts a row-major array of packed `0xAARRGGBB` ints into tightly packed
 * RGBA8888 bytes (R, G, B, A per pixel) for Skia's [ColorType.RGBA_8888].
 *
 * Skia's RGBA_8888 expects bytes in memory order R, G, B, A. Writing a packed
 * `0xAARRGGBB` int directly would land little-endian as B, G, R, A — wrong —
 * so each component is unpacked explicitly.
 */
private fun argbIntsToRgbaBytes(argb: IntArray, width: Int, height: Int): ByteArray {
    val pixelCount = width * height
    val out = ByteArray(pixelCount * 4)
    for (i in 0 until pixelCount) {
        val p = argb[i]
        val base = i * 4
        out[base] = ((p ushr 16) and 0xFF).toByte()     // R
        out[base + 1] = ((p ushr 8) and 0xFF).toByte()  // G
        out[base + 2] = (p and 0xFF).toByte()           // B
        out[base + 3] = ((p ushr 24) and 0xFF).toByte() // A
    }
    return out
}