package com.moly3.cedarjam.shared.di.metro

import com.moly3.cedarjam.core.storage.ISqlStorage

fun interface SqlStorageFactory {
    operator fun invoke(workspacePath: String): ISqlStorage
}
