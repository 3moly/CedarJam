package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalDecoratedWindowScope
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter

@Composable
actual fun CJDraggableArea(modifier: Modifier, content: @Composable (() -> Unit)) {
    WindowDraggableArea(
        modifier = modifier.background(LocalAppTheme.current.primaryColor.copy(alpha = 0.1f))
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WindowDraggableArea(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val window = LocalDecoratedWindowScope.current.window
    val handler = remember { DragHandler(window) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    handler.onDragStarted()
                }
            }
    ) {
        content()
    }
}

fun currentPointerLocation(): IntOffset? {
    return MouseInfo.getPointerInfo()?.location?.toComposeOffset()
}

fun Point.toComposeOffset() = IntOffset(x = x, y = y)

class DragHandler(
    private val window: Window
) {
    private var windowLocationAtDragStart: IntOffset? = null
    private var dragStartPoint: IntOffset? = null

    private val dragListener = object : MouseMotionAdapter() {
        override fun mouseDragged(event: MouseEvent) = onDrag()
    }
    private val removeListener = object : MouseAdapter() {
        override fun mouseReleased(event: MouseEvent) {
            window.removeMouseMotionListener(dragListener)
            window.removeMouseListener(this)
        }
    }

    fun onDragStarted() {
        dragStartPoint = currentPointerLocation() ?: return
        windowLocationAtDragStart = window.location.toComposeOffset()

        window.addMouseListener(removeListener)
        window.addMouseMotionListener(dragListener)
    }

    private fun onDrag() {
        val windowLocationAtDragStart = this.windowLocationAtDragStart ?: return
        val dragStartPoint = this.dragStartPoint ?: return
        val point = currentPointerLocation() ?: return
        val newLocation = windowLocationAtDragStart + (point - dragStartPoint)
        window.setLocation(newLocation.x, newLocation.y)
    }
}