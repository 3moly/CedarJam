package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.FileStructure

expect fun extractZipFromBytes(
    bytes: ByteArray,
    destinationPath: String,
    fileStructure: FileStructure
)