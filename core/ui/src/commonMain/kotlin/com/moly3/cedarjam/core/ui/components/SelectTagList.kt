package com.moly3.cedarjam.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText

@Composable
fun SelectTagList(
    tags: List<TagDTO>,
    currentTag: TagDTO?,
    currentTag2: TagDTO?,
    onSelectTag: (TagDTO) -> Unit
) {
    Column(
        modifier = Modifier
            .height(400.dp)
            .verticalScroll(rememberScrollState())
    ) {
        for (tag in tags) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CJText(text = tag.name)
                if (currentTag?.name != tag.name && currentTag2?.name != tag.name) {
                    CJButton(text = "Select") {
                        onSelectTag(tag)
                    }
                }
            }
        }
    }
}