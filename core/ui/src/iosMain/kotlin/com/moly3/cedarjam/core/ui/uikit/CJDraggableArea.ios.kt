package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CJDraggableArea(modifier: Modifier, content: @Composable (() -> Unit)) {
    content()
}