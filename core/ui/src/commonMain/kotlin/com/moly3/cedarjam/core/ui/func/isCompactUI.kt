package com.moly3.cedarjam.core.ui.func

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.model.WindowSize

@Composable
fun isCompactUI(): Boolean {
    val windowSize by rememberWindowSize()

    return remember(windowSize) {
        windowSize == WindowSize.Compact
    }
}

@Composable
fun pageControlsPadding(): Dp {
    return if (isCompactUI()) {
        50.dp
    } else 0.dp
}