package com.moly3.cedarjam.core.domain.func

import com.moly3.cedarjam.core.domain.model.Platform

actual fun getPlatform(): Platform {
//    val os = System.getProperty("os.name").lowercase()
//    val isWindows = os.contains("win")
//    val isMac = os.contains("mac")
//    val isLinux = os.contains("nux") || os.contains("nix") || os.contains("aix")

    return Platform.Jvm
}