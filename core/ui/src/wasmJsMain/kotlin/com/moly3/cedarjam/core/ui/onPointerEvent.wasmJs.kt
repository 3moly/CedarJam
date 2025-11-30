package com.moly3.cedarjam.core.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onPointerEvent(
    pointerEventType: PointerEventType,
    onEvent: (PointerEvent) -> Unit
): Modifier {
    return this.onPointerEvent(
        eventType = pointerEventType,
        onEvent = {
            onEvent(it)
        })
}