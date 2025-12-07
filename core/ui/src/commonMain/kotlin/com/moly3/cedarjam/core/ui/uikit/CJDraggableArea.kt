package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.runtime.Composable
import com.moly3.cedarjam.core.ui.JvmWindowScope

@Composable
expect fun JvmWindowScope.CJDraggableArea(content: @Composable () -> Unit)