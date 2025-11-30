package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.moly3.cedarjam.core.domain.model.AppSettings
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CJContextMenu(
    modifier: Modifier,
    columnModifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier.zIndex(1_000f)
    ) {
        Column(modifier = columnModifier.padding(6.dp).width(150.dp)) {
            content()
        }
    }
}



@Preview
@Composable
fun ContextMenuPreview() {
    CJApplicationTheme(AppSettings.defaultSettings) {
        CJContextMenu(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
            columnModifier = Modifier
        ) {

        }
    }
}