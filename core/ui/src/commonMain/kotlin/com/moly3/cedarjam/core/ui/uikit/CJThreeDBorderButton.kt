package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CJThreeDBorderButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    cornerRadius: Int = 4,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                Color.Transparent,//if (isEnabled) Color(0xFF2B2B2B) else
                shape = RoundedCornerShape(cornerRadius.dp)
            )

            .let {
                if (isEnabled)
                    it.border(
                        BorderStroke(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF545454), // top lighter
                                    Color(0xFF272727) // bottom darker
                                ),
                                endY = 50f
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius.dp)
                    )
                else
                    it
            }
            .clip(RoundedCornerShape(cornerRadius.dp))
            .let {
                if (isEnabled)
                    it.clickable { onClick() }
                else
                    it
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
fun Three3PReview() {
    CJThreeDBorderButton(cornerRadius = 4) {
        CJText("arrow")
    }
}