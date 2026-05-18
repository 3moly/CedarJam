package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CJButtSnap(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    content: (@Composable BoxScope.(Color) -> Unit)? = null,
    isSelected: Boolean,
    buttType: ButtSnapType = ButtSnapType.Center,
    onClick: () -> Unit
) {
    val shape = remember(buttType) {
        when (buttType) {
            ButtSnapType.Start -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            ButtSnapType.Center -> RoundedCornerShape(0.dp)
            ButtSnapType.End -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        }
    }
    NeumorphicShape(
        modifier = modifier
            .height(32.dp)
            .widthIn(min = 42.dp)
            .background(Color.Transparent, shape),
        isPressed = isSelected,
        isEnabled = !isSelected,
        buttonShape = shape,
        painter = painter,
        content = content,
        onClick = {
            onClick()
        }
    )
}

@Preview
@Composable
fun ButtSnapPreview() {
    AppThemePreview {
        Row {
            CJButtSnap(
                painter = rememberVectorPainter(vector.BarLeft),
                isSelected = false,
                buttType = ButtSnapType.Start
            ) {}
            CJButtSnap(
                painter = rememberVectorPainter(vector.NetworkNode),
                isSelected = true,
                buttType = ButtSnapType.Center
            ) {}
            CJButtSnap(
                isSelected = false,
                content = {
                    CJText(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = "annotations",
                        color = it
                    )
                },
                buttType = ButtSnapType.Center
            ) {}
            CJButtSnap(
                painter = rememberVectorPainter(vector.Tag),
                isSelected = false,
                buttType = ButtSnapType.End
            ) {}
        }
    }

}

enum class ButtSnapType {
    Start,
    Center,
    End
}