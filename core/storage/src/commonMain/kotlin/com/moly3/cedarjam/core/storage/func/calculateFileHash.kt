package com.moly3.cedarjam.core.storage.func

import okio.HashingSource
import okio.Path.Companion.toPath
import okio.blackholeSink
import okio.buffer
import okio.use

fun calculateFileHash(filePath: String): String {
    val path = filePath.toPath()
    getOkioFileSystem(path).use { fileSource ->
        HashingSource.sha256(fileSource).use { hashingSource ->
            hashingSource.buffer().readAll(blackholeSink())
            return hashingSource.hash.hex()
        }
    }
}