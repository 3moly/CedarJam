package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.model.Platform

val hiddenDirectory: String
    get() {
        return when (getPlatform()) {
            Platform.Android -> "moly3"
            Platform.Ios -> "moly3"
            is Platform.Jvm -> "moly3"
            Platform.Wasm -> "moly3"
        }
    }

val dsStoreFile: String = ".DS_Store"