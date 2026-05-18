package com.moly3.cedarjam.core.domain.func

expect suspend fun shareFile(fullPath: String)
expect suspend fun shareBytes(bytes: ByteArray)
