package com.moly3.cedarjam.core.storage.func

import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.internal.SystemFilesManager

actual fun createSystemFilesManager(): ISystemFilesManager {
    return SystemFilesManager()
}