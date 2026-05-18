package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CJToggler(
    modifier: Modifier,
    list: ImmutableList<String>,
    onDisplay: @Composable (String) -> String,
    selected: String,
    onSelect: (String) -> Unit
) {
    val colors = LocalAppTheme.current.colors
    val selectedIndex = remember(list, selected) {
        list.indexOf(selected)
    }
    val cellWidth = 60
    val animWidth by animateDpAsState((selectedIndex * cellWidth).dp)
    Box(
        modifier = modifier
            .background(LocalAppTheme.current.colors.backgroundSecondary, RoundedCornerShape(8.dp))
            .height(32.dp)
            .border(volumedBorderStroke, RoundedCornerShape(8.dp))
            .padding(2.dp)

    ) {
        //.padding(horizontal = 2.dp, vertical = 2.dp)
        Box(
            modifier = Modifier
                .padding(start = animWidth)
                .width(cellWidth.dp)
                .fillMaxHeight()
                .border(volumedBorderStroke, RoundedCornerShape(12.dp))
        ) {

        }
        Row {
            for (item in list) {
                val textColor = remember(item, selected, colors) {
                    if (item == selected) {
                        colors.primaryFont
                    } else {
                        colors.secondaryFont
                    }
                }
                val animateColor by animateColorAsState(textColor)
                Box(
                    modifier = Modifier
                        .width(cellWidth.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelect(item)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CJText(text = onDisplay(item), fontSize = 12.sp, color = animateColor)
                }
            }
        }
    }
}