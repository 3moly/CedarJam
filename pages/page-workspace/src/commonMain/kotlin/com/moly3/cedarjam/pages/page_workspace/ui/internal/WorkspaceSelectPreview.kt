package com.moly3.cedarjam.pages.page_workspace.ui.internal

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJIcon
import com.moly3.cedarjam.core.ui.vectors.Bulb
import com.moly3.cedarjam.core.ui.vectors.SettingsFuture
import com.moly3.cedarjam.core.ui.vectors.WaterDrop
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun WorkspaceSelectPreview() {
    WorkspaceSelect(null, onChangeSettings = {}, onChangeWorkspace = {}, onChangeColors = {})
}

@Composable
fun WorkspaceSelect(
    activeWorkspace: WorkspacePresentation?,
    onChangeWorkspace: () -> Unit,
    onChangeSettings: () -> Unit,
    onChangeColors: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(volumedBorderStroke, shape = RoundedCornerShape(8.dp)).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clickable {
                    onChangeWorkspace()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.weight(1f)) {
                CJText(
                    modifier = Modifier.weight(1f),
                    text = activeWorkspace?.name ?: "no workspace",
                    color = LocalAppTheme.current.colors.primaryFont,
                    softWrap = false
                )
            }
        }
        CJIcon(
            modifier = Modifier,
            painter = rememberVectorPainter(WaterDrop),
            isEnabled = true,
            onClick = onChangeSettings
        )
        CJIcon(
            modifier = Modifier,
            painter = rememberVectorPainter(Bulb),
            isEnabled = true,
            onClick = onChangeColors
        )
    }
}
