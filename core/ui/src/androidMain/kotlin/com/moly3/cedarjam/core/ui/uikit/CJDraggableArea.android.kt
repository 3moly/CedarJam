package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.ui.JvmWindowScope

@Composable
actual fun CJDraggableArea(modifier: Modifier, content: @Composable (() -> Unit)) {
    content()
}