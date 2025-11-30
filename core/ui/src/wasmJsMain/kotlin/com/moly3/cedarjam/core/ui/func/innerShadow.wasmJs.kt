package com.moly3.cedarjam.core.ui.func

@androidx.compose.runtime.Composable
actual fun androidx.compose.ui.Modifier.innerShadow(
    shape: androidx.compose.ui.graphics.Shape,
    color: androidx.compose.ui.graphics.Color,
    blur: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
    offsetX: androidx.compose.ui.unit.Dp,
    spread: androidx.compose.ui.unit.Dp
): androidx.compose.ui.Modifier {
    return this
}