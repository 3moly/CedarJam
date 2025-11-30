package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState

@Composable
fun WindowScope.MacWindowControls(
    windowState: WindowState,
    onClose: () -> Unit
) {
    WindowDraggableArea(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ControlButton(color = Color(0xFFFF5F56), onClick = onClose)
            ControlButton(color = Color(0xFFFFBD2E), onClick = {
                windowState.isMinimized = true
            })
            ControlButton(color = Color(0xFF27C93F), onClick = {
                windowState.placement = if (windowState.placement == WindowPlacement.Fullscreen)
                    WindowPlacement.Floating
                else
                    WindowPlacement.Fullscreen

            })
        }
    }
}

@Composable
fun ControlButton(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(12.dp)
            .background(color, shape = RoundedCornerShape(12.dp))
            .clickable {
                onClick()
            }
            .clip(RoundedCornerShape(12.dp))
    ) {}
}