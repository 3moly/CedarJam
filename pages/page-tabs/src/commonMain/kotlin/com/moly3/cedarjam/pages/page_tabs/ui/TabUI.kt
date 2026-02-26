package com.moly3.cedarjam.pages.page_tabs.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJButtonIcon
import com.moly3.cedarjam.core.ui.uikit.CJText
import vectors.CloseSM

@Composable
fun TabUI(
    isActive: Boolean,
    isSelected: Boolean,
    icon: Painter?,
    name: String,
    onClick: () -> Unit,
    onFileTreeReveal: () -> Unit,
    onRemove: () -> Unit
) {
    val isBorder = remember(isActive, isSelected) {
        isActive && isSelected
    }
    Row(
        Modifier
            .width(200.dp)
            .background(
                if (isSelected) LocalAppTheme.current.colors.backgroundSecondary else LocalAppTheme.current.colors.backgroundPrimary,
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .let {
                if (isBorder) {
                    it.border(
                        1.dp,
                        LocalAppTheme.current.primaryColor,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                } else {
                    it
                }
            }
            .let {
                if (isSelected)
                    it
                else
                    it.clickable {
                        onClick()
                    }
            }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Image(
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        onFileTreeReveal()
                    },
                painter = icon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
            )
        }
        CJText(
            modifier = Modifier.weight(1f),
            text = name,
            color = LocalAppTheme.current.colors.primaryFont,
            fontSize = 10.sp,
            maxLines = 1
        )
        CJButtonIcon(
            modifier = Modifier,
            size = 16,
            imageVector = CloseSM,
            onClick = {
                onRemove()
            })
    }
}

@Preview
@Composable
fun TabUIPreview() {
    TabUI(
        isSelected = true,
        name = "Untitled",
        onClick = {},
        onRemove = {},
        icon = null,
        isActive = false,
        onFileTreeReveal = {}
    )
}