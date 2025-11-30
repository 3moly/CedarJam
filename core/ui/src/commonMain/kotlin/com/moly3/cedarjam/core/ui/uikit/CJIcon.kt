package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

@Composable
fun CJIcon(
    modifier: Modifier = Modifier,
    painter: Painter,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    CJThreeDBorderButton(
        modifier = modifier
            .width(28.dp)
            .height(24.dp),
        isEnabled = isEnabled,
        cornerRadius = 4,
        onClick = onClick
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon.copy(alpha = if (isEnabled) 1f else 0.5f))
        )
    }
}