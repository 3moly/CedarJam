package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme

@Composable
fun CJDivider(
    modifier: Modifier = Modifier,
    color: Color = LocalAppTheme.current.colors.divide
) {
    Box(
        modifier.fillMaxWidth().height(1.dp).background(color = color)
    )
}