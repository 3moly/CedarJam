package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.model.WindowSize

@Composable
fun rememberWindowSize(): State<WindowSize> {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return remember(windowInfo, density) {
        derivedStateOf {
            val widthDp = with(density) { windowInfo.containerSize.width.toDp() }
            when {
                widthDp < 600.dp -> WindowSize.Compact
                widthDp < 840.dp -> WindowSize.Medium
                else -> WindowSize.Expanded
            }
        }
    }
}