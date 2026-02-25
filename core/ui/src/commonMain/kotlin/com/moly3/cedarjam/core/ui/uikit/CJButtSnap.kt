package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CJButtSnap(
    modifier: Modifier = Modifier,
    painter: Painter,
    isSelected: Boolean,
    buttType: ButtSnapType = ButtSnapType.Center,
    onClick: () -> Unit
) {
    val shape = remember(buttType) {
        when (buttType) {
            ButtSnapType.Left -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            ButtSnapType.Center -> RoundedCornerShape(0.dp)
            ButtSnapType.Right -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        }
    }
    NeumorphicShape(
        modifier = modifier
            .height(32.dp)
            .width(42.dp)
            .background(Color.Transparent, shape),
        isPressed = isSelected,
        isEnabled = !isSelected,
        buttonShape = shape,
        painter = painter,
        onClick = {
            onClick()
        }
    )
//    Box(
//        modifier = modifier
//            .height(32.dp)
//            .width(42.dp)
//            .background(Color.Transparent, shape)
//            .border(BorderStroke(1.dp, borderColor), shape = shape)
//            .clip(shape)
//            .clickable(enabled = !isSelected) {
//                onClick()
//            }
//    ) {
//        Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
//            Image(
//                modifier = Modifier.size(20.dp),
//                painter = painter,
//                contentDescription = null,
//                colorFilter = ColorFilter.tint(borderColor)
//            )
//        }
//    }
}

@Preview
@Composable
fun ButtSnapPreview() {
    Row {
//        ButtSnap(
//            painter = painterResource(MR.images.img_flower),
//            isSelected = false,
//            buttType = ButtSnapType.Left
//        ) {}
//        ButtSnap(
//            painter = painterResource(MR.images.img_flower),
//            isSelected = true,
//            buttType = ButtSnapType.Center
//        ) {}
//        ButtSnap(
//            painter = painterResource(MR.images.img_flower),
//            isSelected = false,
//            buttType = ButtSnapType.Right
//        ) {}
    }
}

enum class ButtSnapType {
    Left,
    Center,
    Right
}