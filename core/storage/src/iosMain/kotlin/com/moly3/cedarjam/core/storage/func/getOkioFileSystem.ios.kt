package com.moly3.cedarjam.core.storage.func

import okio.FileSystem
import okio.Path
import okio.Source

actual fun getOkioFileSystem(path: Path): Source {
    return FileSystem.SYSTEM.source(path)
}