package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.moly3.cedarjam.core.ui.model.WindowSize

@Composable
fun isCompactUI(): Boolean {
    val windowSize by rememberWindowSize()

    return remember(windowSize) {
        windowSize == WindowSize.Compact
    }
}