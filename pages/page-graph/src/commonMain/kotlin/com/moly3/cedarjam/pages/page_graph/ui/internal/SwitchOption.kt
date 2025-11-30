package com.moly3.cedarjam.pages.page_graph.ui.internal

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.moly3.cedarjam.core.ui.uikit.CJSwitch
import com.moly3.cedarjam.core.ui.uikit.CJText

@Composable
fun SwitchOption(modifier: Modifier = Modifier, text: String, value: Boolean, onClick: () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CJText(
            text = text,
            modifier = Modifier.weight(1f),
            softWrap = false
        )
        CJSwitch(
            modifier = Modifier,
            checked = value,
            onCheckedChange = {
                onClick()
            })
    }
}