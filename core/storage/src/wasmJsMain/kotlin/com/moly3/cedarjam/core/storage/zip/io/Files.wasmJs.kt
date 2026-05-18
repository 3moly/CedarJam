package com.oldguy.common.io

import com.oldguy.common.io.charsets.Charset
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

actual class TimeZones {
    actual val defaultId: String
        get() = TODO("Not yet implemented")
    actual val kotlinxTz: TimeZone
        get() = TODO("Not yet implemented")

    actual fun localFromEpochMilliseconds(epochMilliseconds: Long): LocalDateTime {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual val default: TimeZone
            get() = TODO("Not yet implemented")
    }
}

actual class File actual constructor(
    filePath: String,
    platformFd: FileDescriptor?
) {
    actual val name: String
        get() = TODO("Not yet implemented")
    actual val nameWithoutExtension: String
        get() = TODO("Not yet implemented")
    actual val extension: String
        get() = TODO("Not yet implemented")
    actual val path: String
        get() = TODO("Not yet implemented")
    actual val fullPath: String
        get() = TODO("Not yet implemented")
    actual val directoryPath: String
        get() = TODO("Not yet implemented")
    actual val isParent: Boolean
        get() = TODO("Not yet implemented")
    actual val isDirectory: Boolean
        get() = TODO("Not yet implemented")
    actual val exists: Boolean
        get() = TODO("Not yet implemented")
    actual val platformFd: FileDescriptor?
        get() = TODO("Not yet implemented")
    actual val isUri: Boolean
        get() = TODO("Not yet implemented")
    actual val isUriString: Boolean
        get() = TODO("Not yet implemented")
    actual val size: ULong
        get() = TODO("Not yet implemented")
    actual val lastModifiedEpoch: Long
        get() = TODO("Not yet implemented")
    actual val lastModified: LocalDateTime?
        get() = TODO("Not yet implemented")
    actual val createdTime: LocalDateTime?
        get() = TODO("Not yet implemented")
    actual val lastAccessTime: LocalDateTime?
        get() = TODO("Not yet implemented")

    actual suspend fun delete(): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun copy(destinationPath: String): File {
        TODO("Not yet implemented")
    }

    actual suspend fun makeDirectory(): File {
        TODO("Not yet implemented")
    }

    actual suspend fun resolve(
        directoryName: String,
        make: Boolean
    ): File {
        TODO("Not yet implemented")
    }

    actual suspend fun directoryList(): List<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun directoryFiles(): List<File> {
        TODO("Not yet implemented")
    }

    actual fun up(): File {
        TODO("Not yet implemented")
    }

    actual fun newFile(): File {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual val pathSeparator: Char
            get() = TODO("Not yet implemented")

        actual fun tempDirectoryPath(): String {
            TODO("Not yet implemented")
        }

        actual fun tempDirectoryFile(): File {
            TODO("Not yet implemented")
        }

        actual fun workingDirectory(): File {
            TODO("Not yet implemented")
        }

        actual val defaultTimeZone: TimeZones
            get() = TODO("Not yet implemented")
    }

    actual constructor(parentDirectory: String, name: String) : this(parentDirectory) {
        TODO("Not yet implemented")
    }

    actual constructor(parentDirectory: File, name: String) : this(name) {
        TODO("Not yet implemented")
    }

    actual constructor(fd: FileDescriptor) : this("fd") {
        TODO("Not yet implemented")
    }
}

actual interface Closeable {
    actual suspend fun close()
}

actual suspend fun <T : Closeable?, R> T.use(body: suspend (T) -> R): R {
    TODO("Not yet implemented")
}

actual class RawFile actual constructor(
    fileArg: File,
    mode: FileMode,
    source: FileSource
) : Closeable {
    actual override suspend fun close() {
    }

    actual val file: File
        get() = TODO("Not yet implemented")
    actual var position: ULong
        get() = TODO("Not yet implemented")
        set(value) {}
    actual val size: ULong
        get() = TODO("Not yet implemented")
    actual var blockSize: UInt
        get() = TODO("Not yet implemented")
        set(value) {}

    actual suspend fun read(
        buf: ByteBuffer,
        reuseBuffer: Boolean
    ): UInt {
        TODO("Not yet implemented")
    }

    actual suspend fun read(
        buf: ByteBuffer,
        newPos: ULong,
        reuseBuffer: Boolean
    ): UInt {
        TODO("Not yet implemented")
    }

    actual suspend fun read(
        buf: UByteBuffer,
        reuseBuffer: Boolean
    ): UInt {
        TODO("Not yet implemented")
    }

    actual suspend fun read(
        buf: UByteBuffer,
        newPos: ULong,
        reuseBuffer: Boolean
    ): UInt {
        TODO("Not yet implemented")
    }

    actual suspend fun readBuffer(length: UInt): ByteBuffer {
        TODO("Not yet implemented")
    }

    actual suspend fun readBuffer(
        length: UInt,
        newPos: ULong
    ): ByteBuffer {
        TODO("Not yet implemented")
    }

    actual suspend fun readUBuffer(length: UInt): UByteBuffer {
        TODO("Not yet implemented")
    }

    actual suspend fun readUBuffer(
        length: UInt,
        newPos: ULong
    ): UByteBuffer {
        TODO("Not yet implemented")
    }

    actual suspend fun setLength(length: ULong) {
    }

    actual suspend fun write(buf: ByteBuffer) {
    }

    actual suspend fun write(buf: ByteBuffer, newPos: ULong) {
    }

    actual suspend fun write(buf: UByteBuffer) {
    }

    actual suspend fun write(buf: UByteBuffer, newPos: ULong) {
    }

    actual suspend fun copyTo(
        destination: RawFile,
        blockSize: Int,
        transform: ((buffer: ByteBuffer, lastBlock: Boolean) -> ByteBuffer)?
    ): ULong {
        TODO("Not yet implemented")
    }

    actual suspend fun copyToU(
        destination: RawFile,
        blockSize: Int,
        transform: ((buffer: UByteBuffer, lastBlock: Boolean) -> UByteBuffer)?
    ): ULong {
        TODO("Not yet implemented")
    }

    actual suspend fun transferFrom(
        source: RawFile,
        startIndex: ULong,
        length: ULong
    ): ULong {
        TODO("Not yet implemented")
    }

    actual suspend fun truncate(size: ULong) {
    }
}

actual class TextFile actual constructor(
    file: File,
    charset: Charset,
    mode: FileMode,
    source: FileSource
) : Closeable {
    actual val file: File
        get() = TODO("Not yet implemented")
    actual val charset: Charset
        get() = TODO("Not yet implemented")

    actual override suspend fun close() {
    }

    actual suspend fun forEachLine(action: (count: Int, line: String) -> Boolean) {
    }

    actual suspend fun forEachBlock(
        maxSizeBytes: Int,
        action: (text: String) -> Boolean
    ) {
    }

    actual suspend fun read(maxSizeBytes: Int): String {
        TODO("Not yet implemented")
    }

    actual suspend fun readLine(): String {
        TODO("Not yet implemented")
    }

    actual suspend fun write(text: String) {
    }

    actual suspend fun writeLine(text: String) {
    }

    actual constructor(
        filePath: String,
        charset: Charset,
        mode: FileMode,
        source: FileSource
    ) : this(File(filePath, null), charset, mode, source)
}