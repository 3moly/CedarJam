package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.storage.DemoFilesManagerImpl
import com.moly3.cedarjam.core.storage.ISystemFilesManager

actual fun createSystemFilesManager(): ISystemFilesManager {
    return DemoFilesManagerImpl()
}