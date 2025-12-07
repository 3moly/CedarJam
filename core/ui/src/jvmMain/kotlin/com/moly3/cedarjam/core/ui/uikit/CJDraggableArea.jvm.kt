package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import com.moly3.cedarjam.core.ui.JvmWindowScope

@Composable
actual fun JvmWindowScope.CJDraggableArea(content: @Composable (() -> Unit)) {
    WindowDraggableArea {
        content()
    }
}