package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle

@Composable
fun CJButton(
    modifier: Modifier = Modifier,
    text: String,
    backColor: Color = Color.Black,
    fontColor: Color = LocalAppTheme.current.colors.primaryFont,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(backColor, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        CJText(
            text = text,
            style = LocalTextStyle.current,
            fontSize = 12.sp,
            color = fontColor,
            maxLines = 1
        )
    }
}