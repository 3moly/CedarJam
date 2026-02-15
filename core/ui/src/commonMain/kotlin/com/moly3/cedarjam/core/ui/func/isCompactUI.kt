package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.Platform

@Composable
fun isCompactUI(): Boolean {
//    val windowSize = rememberWindowSize()
    return remember {
        when (getPlatform()) {
            Platform.Android,
            Platform.Ios -> true
            Platform.Jvm,
            Platform.Wasm -> false
        }
    }
}