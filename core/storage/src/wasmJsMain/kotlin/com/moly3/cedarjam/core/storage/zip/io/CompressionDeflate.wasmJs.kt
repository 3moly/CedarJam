package com.oldguy.common.io

actual class CompressionDeflate actual constructor(noWrap: Boolean) :
    Compression {
    actual override val algorithm: CompressionAlgorithms
        get() = TODO("Not yet implemented")
    actual override val bufferSize: Int
        get() = TODO("Not yet implemented")
    actual var strategy: Strategy
        get() = TODO("Not yet implemented")
        set(value) {}
    actual override var zlibHeader: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    actual override suspend fun compress(
        input: suspend () -> ByteBuffer,
        output: suspend (buffer: ByteBuffer) -> Unit
    ): ULong {
        TODO("Not yet implemented")
    }

    actual override suspend fun compressArray(
        input: suspend () -> ByteArray,
        output: suspend (buffer: ByteArray) -> Unit
    ): ULong {
        TODO("Not yet implemented")
    }

    actual override suspend fun decompress(
        input: suspend () -> ByteBuffer,
        output: suspend (buffer: ByteBuffer) -> Unit
    ): ULong {
        TODO("Not yet implemented")
    }

    actual override suspend fun decompressArray(
        input: suspend () -> ByteArray,
        output: suspend (buffer: ByteArray) -> Unit
    ): ULong {
        TODO("Not yet implemented")
    }

    actual enum class Strategy { Default, Filtered, Huffman }
}