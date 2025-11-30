package com.moly3.cedarjam.core.ui.func

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import co.touchlab.kermit.Logger

actual fun Modifier.onSecondaryClickWithPosition(
    key: Any,
    onClick: (Offset?) -> Unit,
    onLongPress: (Offset) -> Unit,
    onSecondaryClick: (Offset) -> Unit
): Modifier {
    return  pointerInput(key) {
        detectTapGestures(
            onLongPress = { offset ->
                onLongPress(offset)
            },
            onTap = { offset ->
                onClick(offset)
            }
        )
    }
}