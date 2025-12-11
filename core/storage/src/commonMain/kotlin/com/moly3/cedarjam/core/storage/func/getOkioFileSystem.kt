package com.moly3.cedarjam.core.storage.func

import okio.Path
import okio.Source

expect fun getOkioFileSystem(path: Path): Source