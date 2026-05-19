package com.moly3.cedarjam.core.decoder

import coil3.decode.Decoder
import coil3.decode.ImageSource
import okio.ByteString.Companion.encodeUtf8
import okio.use

/*
 * commonMain
 *
 * Shared HEIC detection + the `expect` entry point. Each platform supplies an
 * `actual fun heicDecoderFactory()` in its own source set:
 *
 *   androidMain -> AndroidHeicDecoder  (platform BitmapFactory, API 28+)
 *   iosMain     -> IosHeicDecoder      (ImageIO / CGImageSource)
 *   jvmMain     -> JvmHeicDecoder      (Openize.HEIC, pure-JVM)
 *
 * Register the result on your ImageLoader:
 *
 *   ImageLoader.Builder(context)
 *       .components {
 *           heicDecoderFactory()?.let { add(it) }
 *       }
 *       .build()
 *
 * `heicDecoderFactory()` is nullable so registration degrades safely when a
 * platform genuinely cannot decode HEIC (e.g. Android < API 28).
 */

/**
 * Entry point. Returns a Coil [Decoder.Factory] that decodes HEIC/HEIF, or
 * `null` if this platform cannot decode HEIC.
 */
expect fun heicDecoderFactory(): Decoder.Factory?


/**
 * HEIF/HEIC brand identifiers found in the ISO Base Media File Format `ftyp`
 * box.
 *
 * A HEIC file is an ISO-BMFF container. The first box is always `ftyp`:
 *
 * ```
 * offset 0..3   : box size (uint32, big-endian)
 * offset 4..7   : box type  -> always the ASCII string "ftyp"
 * offset 8..11  : major brand (4cc)
 * offset 12..15 : minor version
 * offset 16..   : zero or more compatible brands (4cc each)
 * ```
 *
 * A file is treated as HEIF if the major brand OR any compatible brand matches
 * one of the known still-image / sequence brands below.
 */
private val HEIF_BRANDS: Set<String> = setOf(
    // HEVC-coded still images
    "heic", "heix", "heim", "heis",
    // HEVC-coded image sequences
    "hevc", "hevx", "hevm", "hevs",
    // Generic HEIF still / sequence containers
    "mif1", "msf1", "mif2",
)

private const val FTYP = "ftyp"

/** Number of header bytes we peek to make a confident decision. */
private const val HEADER_PEEK_BYTES = 64L

/** A valid `ftyp` box is at least size(4) + type(4) + major(4) = 12 bytes. */
private const val MIN_FTYP_BYTES = 12

/**
 * Returns true if the bytes at the head of [source] look like a HEIF/HEIC file.
 *
 * This does NOT consume the source: it uses Okio's `peek()` so that, if this
 * decoder declines the image, the next [Decoder.Factory] in the registry still
 * sees an intact stream.
 *
 * Never rely on the file extension: many HEIC files arrive over the network
 * with no extension, or are mislabelled `.jpg` by camera apps.
 */
fun isHeic(source: ImageSource): Boolean {
    // Fast path: peek a fixed-size header.
    runCatching {
        source.source().peek().use { peeked ->
            peeked.require(HEADER_PEEK_BYTES)
            peeked.readByteArray(HEADER_PEEK_BYTES)
        }
    }.getOrNull()?.let { return it.matchesHeif() }

    // Slow path: stream shorter than HEADER_PEEK_BYTES. A valid ftyp box is
    // only 12 bytes, so retry with whatever the stream actually has.
    return runCatching {
        source.source().peek().use { it.readByteArray() }
    }.getOrNull()?.matchesHeif() ?: false
}

private fun ByteArray.matchesHeif(): Boolean {
    if (size < MIN_FTYP_BYTES) return false

    // bytes 4..7 must spell "ftyp"
    if (!regionEquals(offset = 4, ascii = FTYP)) return false

    // major brand at 8..11
    if (asciiAt(8) in HEIF_BRANDS) return true

    // Compatible brands run from offset 16 up to the declared ftyp box size.
    //
    // ISO-BMFF box-size special cases:
    //   size == 0 -> box extends to end of file
    //   size == 1 -> 64-bit extended size follows the type field
    // In either case we cannot trust the 32-bit value as an in-buffer end
    // offset, so we scan the whole peeked buffer instead.
    val declaredSize = readUInt32BE(0)
    val end = when (declaredSize) {
        0, 1 -> size
        else -> minOf(declaredSize, size)
    }

    var offset = 16
    while (offset + 4 <= end) {
        if (asciiAt(offset) in HEIF_BRANDS) return true
        offset += 4
    }
    return false
}

private fun ByteArray.regionEquals(offset: Int, ascii: String): Boolean {
    val target = ascii.encodeUtf8()
    if (offset < 0 || offset + target.size > size) return false
    for (i in 0 until target.size) {
        if (this[offset + i] != target[i]) return false
    }
    return true
}

private fun ByteArray.asciiAt(offset: Int): String {
    if (offset < 0 || offset + 4 > size) return ""
    val chars = CharArray(4) { i -> (this[offset + i].toInt() and 0xFF).toChar() }
    return chars.concatToString()
}

/**
 * Reads a big-endian uint32 at [offset]. Returns 0 when there aren't 4 bytes
 * available — callers must treat 0 as "size unknown" (the ISO-BMFF
 * size-extends-to-EOF sentinel), which [matchesHeif] does.
 */
private fun ByteArray.readUInt32BE(offset: Int): Int {
    if (offset < 0 || offset + 4 > size) return 0
    return ((this[offset].toInt() and 0xFF) shl 24) or
            ((this[offset + 1].toInt() and 0xFF) shl 16) or
            ((this[offset + 2].toInt() and 0xFF) shl 8) or
            (this[offset + 3].toInt() and 0xFF)
}