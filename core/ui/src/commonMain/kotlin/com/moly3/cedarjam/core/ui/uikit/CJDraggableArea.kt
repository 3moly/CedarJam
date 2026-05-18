package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CJDraggableArea(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit)