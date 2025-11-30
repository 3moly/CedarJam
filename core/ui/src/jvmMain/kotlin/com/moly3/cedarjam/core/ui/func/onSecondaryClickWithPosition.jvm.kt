package com.moly3.cedarjam.core.ui.func

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.onSecondaryClickWithPosition(
    key: Any,
    onClick: (Offset?) -> Unit,
    onLongPress: (Offset) -> Unit,
    onSecondaryClick: (Offset) -> Unit
): Modifier {
    return this.clickable {
        onClick(null)
    }.pointerInput(key) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()

                if (event.type == PointerEventType.Press) {
                    val position = event.changes.firstOrNull()?.position ?: Offset.Zero

                    if (event.buttons.isSecondaryPressed) {
                        onSecondaryClick(position)
                    }
                }
            }
        }
    }
}