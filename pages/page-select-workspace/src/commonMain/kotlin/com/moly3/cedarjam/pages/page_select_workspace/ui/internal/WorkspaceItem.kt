package com.moly3.cedarjam.pages.page_select_workspace.ui.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.AppThemePreview
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import com.moly3.cedarjam.core.ui.vectors.DotsHorizontal
import com.moly3.cedarjam.core.ui.vectors.TrashEmpty
import com.moly3.cedarjam.pages.page_select_workspace.Intent

@Composable
internal fun WorkspaceItem(
    workspace: WorkspacePresentation,
    onIntent: (Intent) -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    NeumorphicShape(
        modifier = Modifier.fillMaxWidth(),
        buttonShape = shape,
        content = {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CJText(text = workspace.name, fontSize = 16.sp)
                    CJText(
                        text = workspace.fullpath,
                        fontSize = 12.sp,
                        color = LocalAppTheme.current.colors.secondaryFont
                    )
                }
                NeumorphicShape(
                    modifier = Modifier.size(40.dp),
                    painter = rememberVectorPainter(DotsHorizontal),
                   ) {

                }
            }
        },
        onClick = {
            onIntent(Intent.SelectWorkspace(workspace))
        }
    )
}

@Preview
@Composable
fun WorkspaceItemPreview() {
    AppThemePreview {
        WorkspaceItem(
            WorkspacePresentation(
                name = "3moly",
                serverName = "",
                fullpath = "/Users/new07/Library/Application Support/CedarJam/workspaces/sombra"
            ),
            onIntent = {})
    }
}

@Preview
@Composable
fun WorkspaceItemPreviewLight() {
    AppThemePreview(isDark = false) {
        WorkspaceItem(
            WorkspacePresentation(
                name = "3moly",
                serverName = "",
                fullpath = "/Users/new07/Library/Application Support/CedarJam/workspaces/sombra"
            ),
            onIntent = {})
    }
}