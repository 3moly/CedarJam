package com.moly3.cedarjam.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import org.jetbrains.skia.Image
import org.jetbrains.skia.EncodedImageFormat

/**
 * only works on - jvm, wasmJs
 */
expect fun Modifier.onPointerEvent(
    pointerEventType: PointerEventType,
    onEvent: (PointerEvent) -> Unit
): Modifier