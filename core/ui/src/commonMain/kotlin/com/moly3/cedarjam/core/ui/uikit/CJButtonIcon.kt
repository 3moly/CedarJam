package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

@Composable
fun CJButtonIcon(
    modifier: Modifier = Modifier,
    size: Int = 32,
    imageVector: ImageVector,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    NeumorphicShape(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isEnabled) LocalAppTheme.current.colors.backgroundSecondary else Color.Transparent)
            .let {
                if (isEnabled)
                    it.clickable { onClick() }
                else
                    it
            },
        content = {
            Image(
                imageVector,
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
            )
        }
    )
}

