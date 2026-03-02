package com.moly3.cedarjam.features.feature_graph.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.uikit.CJText

@Composable
fun RowCount(
    modifier: Modifier = Modifier,
    count: Int,
    image: ImageVector,
    color: Color
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = rememberVectorPainter(image),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color)
        )
        CJText(text = "$count", color = color, fontSize = 12.sp, maxLines = 1)
    }
}