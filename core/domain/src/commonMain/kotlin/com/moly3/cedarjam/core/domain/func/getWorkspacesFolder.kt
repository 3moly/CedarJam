package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.model.Platform

fun getWorkspacesFolder(): String {
    return when (getPlatform()) {
        Platform.Android,
        Platform.Ios -> "workspaces"

        Platform.Jvm,
        Platform.Wasm -> ""
    }
}