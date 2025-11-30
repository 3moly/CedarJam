package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import io.github.vinceglb.filekit.FileKit

expect fun FileKit.filesDirPath(): String
expect fun FileKit.init(androidApplicationContext: AndroidApplicationContext)
