package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphPresentation
import vectors.TrashEmpty
import com.moly3.cedarjam.core.ui.volumedBorderStroke

@Composable
fun CJGraphPresentationUI(
    modifier: Modifier = Modifier,
    data: ObsidianGraphPresentation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(volumedBorderStroke, shape = RoundedCornerShape(4.dp))
            .clickable {
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            when (data) {
                is ObsidianGraphPresentation.Collection -> {
                    CJText(text = "Collection ${data.value}")
                }

                is ObsidianGraphPresentation.CollectionRow -> {
                    CJText(text = "CollectionRow ${data.value}")
                }

                is ObsidianGraphPresentation.File -> {
                    CJText(text = "File ${data.value}")
                }

                is ObsidianGraphPresentation.Tag -> {
                    CJText(text = "#${data.value.name}")
                }

                is ObsidianGraphPresentation.Unknown -> {
                    CJText(text = "Unknown: ${data.value}")
                }
            }
        }
        CJButtonIcon(imageVector = TrashEmpty, onClick = {
            onDelete()
        })
    }
}