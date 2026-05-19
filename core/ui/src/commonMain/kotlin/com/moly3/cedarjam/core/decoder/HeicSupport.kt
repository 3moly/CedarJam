package com.moly3.cedarjam.core.decoder

import coil3.decode.ImageSource
import okio.ByteString.Companion.encodeUtf8
import okio.use

/**
 * HEIF/HEIC brand identifiers found in the ISO Base Media File Format `ftyp` box.
 *
 * A HEIC file is an ISO-BMFF container. The first box is always `ftyp`, whose
 * layout is:
 *
 * ```
 * offset 0..3   : box size (uint32, big-endian)
 * offset 4..7   : box type  -> always the ASCII string "ftyp"
 * offset 8..11  : major brand (4cc)
 * offset 12..15 : minor version
 * offset 16..   : zero or more compatible brands (4cc each)
 * ```
 *
 * We treat a file as HEItC if either the major brand OR any compatible brand
 * matches one of the known still-image / sequence brands below.
 */
private val HEIF_BRANDS: Set<String> = setOf(
    // HEVC-coded still images
    "heic", "heix", "heim", "heis",
    // HEVC-coded image sequences
    "hevc", "hevx", "hevm", "hevs",
    // Generic HEIF still / sequence containers
    "mif1", "msf1",
    // Some camera vendors tag bursts/live-photos with these
    "mif2",
)

private const val FTYP = "ftyp"

/** Number of header bytes we peek to make a confident decision. */
private const val HEADER_PEEK_BYTES = 64L

/**
 * Returns true if the bytes at the head of [source] look like a HEIF/HEIC file.
 *
 * This does NOT consume the source. It uses Okio's `peek()` so that, if this
 * decoder declines the image, the next [coil3.decode.Decoder.Factory] in the
 * registry still sees an intact stream.
 *
 * Never rely on the file extension: many HEIC files arrive over the network
 * with no extension, or are mislabelled `.jpg` by camera apps.
 */
//fun isHeic(source: ImageSource): Boolean {
//    val bytes = try {
//        source.source().peek().use { peeked ->
//            peeked.require(HEADER_PEEK_BYTES) // throws if stream is shorter
//            peeked.readByteArray(HEADER_PEEK_BYTES)
//        }
//    } catch (_: Exception) {
//        // Stream shorter than HEADER_PEEK_BYTES, or otherwise unreadable.
//        // Retry with whatever is available — a valid ftyp box is only 12 bytes.
//        return isHeicLenient(source)
//    }
//    return bytes.matchesHeif()
//}

//private fun isHeicLenient(source: ImageSource): Boolean {
//    val bytes = try {
//        source.source().peek().use { it.readByteArray() }
//    } catch (_: Exception) {
//        return false
//    }
//    return bytes.matchesHeif()
//}

//private fun ByteArray.matchesHeif(): Boolean {
//    if (size < 12) return false
//    // bytes 4..7 must spell "ftyp"
//    if (!regionEquals(offset = 4, ascii = FTYP)) return false
//
//    // major brand at 8..11
//    val major = asciiAt(8)
//    if (major in HEIF_BRANDS) return true
//
//    // compatible brands run from offset 16 up to the declared ftyp box size.
//    val boxSize = readUInt32BE(0)
//    val end = minOf(boxSize, size)
//    var offset = 16
//    while (offset + 4 <= end) {
//        if (asciiAt(offset) in HEIF_BRANDS) return true
//        offset += 4
//    }
//    return false
//}

//private fun ByteArray.regionEquals(offset: Int, ascii: String): Boolean {
//    val target = ascii.encodeUtf8()
//    if (offset + target.size > size) return false
//    for (i in 0 until target.size) {
//        if (this[offset + i] != target[i]) return false
//    }
//    return true
//}
//
//private fun ByteArray.asciiAt(offset: Int): String {
//    if (offset + 4 > size) return ""
//    val chars = CharArray(4) { i -> (this[offset + i].toInt() and 0xFF).toChar() }
//    return chars.concatToString()
//}
//
//private fun ByteArray.readUInt32BE(offset: Int): Int {
//    if (offset + 4 > size) return size
//    return ((this[offset].toInt() and 0xFF) shl 24) or
//            ((this[offset + 1].toInt() and 0xFF) shl 16) or
//            ((this[offset + 2].toInt() and 0xFF) shl 8) or
//            (this[offset + 3].toInt() and 0xFF)
//}