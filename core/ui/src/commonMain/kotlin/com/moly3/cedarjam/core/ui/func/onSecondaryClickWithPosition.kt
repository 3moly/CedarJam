package com.moly3.cedarjam.core.ui.func

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

expect fun Modifier.onSecondaryClickWithPosition(
    key: Any,
    onClick: (Offset?) -> Unit,
    onLongPress: (Offset) -> Unit,
    onSecondaryClick: (Offset) -> Unit
): Modifier

//fun Modifier.onSecondaryClickWithPosition(
//    scope: CoroutineScope,
//    key1: Any,
//    onClick: (Offset) -> Unit,
//    onLongClick: (Offset) -> Unit,
//): Modifier = pointerInput(key1) {
//    scope.launch {
//        detectTapGestures(
//            onLongPress = { offset ->
//                onLongClick( offset)
//            },
//            onTap = { offset ->
//                println("Tapped at $offset")
//            },
//            on
//        )
//    }
//    awaitPointerEventScope {
//        while (true) {
//            val event = awaitPointerEvent()
//            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
//                val position = event.changes.firstOrNull()?.position ?: Offset.Zero
//                onClick(position)
//            }
//        }
//    }
//}

//fun Modifier.onSecondaryClickWithPosition(
//    key: Any,
//    onClick: (Offset) -> Unit,
//    onLongClick: (Offset) -> Unit,
//): Modifier = pointerInput(key) {
//    awaitPointerEventScope {
//        while (true) {
//            val event = awaitPointerEvent()
//            val change = event.changes.firstOrNull() ?: continue
//
//            if (event.type == PointerEventType.Press) {
//                when {
//                    event.buttons.isSecondaryPressed -> {
//                        onClick(change.position)
//                    }
//                    event.buttons.isPrimaryPressed -> {
//                        // start long press detection
//                        val down = change.uptimeMillis
//                        // wait until released or time exceeded
//                        while (change.pressed) {
//                            val move = awaitPointerEvent()
//                            val elapsed = move.changes.first().uptimeMillis - down
//                            if (elapsed > 2000L.toLong()) {
//                                onLongClick(change.position)
//                                break
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}