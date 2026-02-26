package com.moly3.cedarjam.features.feature_graph.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalUIConfig
import com.moly3.cedarjam.core.ui.func.flatClickable
import com.moly3.cedarjam.core.ui.service.KVibrator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import vectors.Tag
import com.moly3.cedarjam.core.ui.volumedBorderStroke

@Composable
fun SelectOption(
    modifier: Modifier,
    isOpened: Boolean,
    count: Int?,
    onSetIsShowGraph: (Boolean) -> Unit
) {
    NeumorphicShape(
        modifier = modifier.padding(16.dp).size(LocalUIConfig.current.fabCircleSize).flatClickable{
            KVibrator.vibrateShort()

            onSetIsShowGraph(!isOpened)
        },
//        unpressedColor = LocalAppTheme.current.primaryColor,
        unpressedIconColor = Color.White,
        isPressed = isOpened,
        buttonShape = RoundedCornerShape(100.dp),
        painter = rememberVectorPainter(Tag),
        accentColor = Color(0xFF222325),
//        accentColor = Color.White// Color(0xFFFF916D)
    )
//    Box(
//        modifier = modifier
//
//            .height(60.dp)
//            .border(volumedBorderStroke, RoundedCornerShape(8.dp))
//            .padding(horizontal = 8.dp, vertical = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier,
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            SelectButton(
//                isActive = !isOpened,
//                imageVector = Tag,
//                onClick = {
//                    onSetIsShowGraph(false)
//                }
//            )
//            SelectButton(
//                isActive = isOpened,
//                imageVector = NetworkNode,
//                count = count,
//                onClick = {
//                    onSetIsShowGraph(true)
//                }
//            )
//        }
//    }
}

@Composable
fun SelectButton(
    isActive: Boolean,
    imageVector: ImageVector,
    count: Int? = null,
    onClick: () -> Unit
) {
    val appTheme = LocalAppTheme.current.primaryColor
    Box(
        modifier = Modifier
            .size(50.dp)
            .let {
                if (isActive) {
                    it.border(1.dp, appTheme, RoundedCornerShape(6.dp))
                } else {
                    it.border(volumedBorderStroke, RoundedCornerShape(6.dp))
                }
            }
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                onClick()
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Image(
                imageVector = imageVector,
                contentDescription = null,
                colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon),
                modifier = Modifier.size(22.dp)
            )
            if (count != null) {
                CJText(text = count.toString(), fontSize = 10.sp)
            }
        }
    }
}