package com.moly3.cedarjam.features.feature_settings.child.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.drawUnder
import com.moly3.cedarjam.core.ui.uikit.CJText
import vectors.Right2

@Composable
fun SelectOption(isShowBorder: Boolean, text: String, onClick: () -> Unit) {
    SelectOption(
        modifier = Modifier.clickable {
            onClick()
        },
        isShowBorder = isShowBorder,
        text = text,
        content = {
            Image(
                painter = rememberVectorPainter(Right2),
                contentDescription = null,
                modifier = Modifier.size(24.dp).align(Alignment.CenterEnd),
                colorFilter = ColorFilter.tint(LocalAppTheme.current.colors.icon)
            )
        }
    )
}

@Composable
fun SelectOption(
    modifier: Modifier,
    isShowBorder: Boolean,
    text: String,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .height(44.dp)
            .fillMaxWidth()
            .let {
                if (isShowBorder)
                    it.drawUnder(borderThickness = 0.5.dp)
                else
                    it
            }
//            .clickable {
//                onClick()
//            }
            .padding(horizontal = 24.dp)) {
        CJText(modifier = Modifier.align(Alignment.CenterStart), text = text)
        content()
    }
}