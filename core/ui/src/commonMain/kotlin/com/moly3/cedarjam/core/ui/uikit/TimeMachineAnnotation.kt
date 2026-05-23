package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.TimeMachine
import com.moly3.cedarjam.core.ui.func.dpSize
import com.moly3.cedarjam.core.ui.uikit.AppThemePreview
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.TimeMachineItem
import vector.TrashEmpty

@Composable
internal fun TimeMachineAnnotation(
    workspaceFullPath: String,
    item: TimeMachine.Annotation
) {
    val ful = remember(workspaceFullPath, item.annotation.id) {
        pathWrapper(
            workspaceFullPath,
            hiddenDirectory,
            "image_cache",
            "annotation_${item.annotation.id}.png"
        ).pathString
    }
    Row(
        Modifier.padding(horizontal = 16.dp).fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            modifier = Modifier.width(75.dp),
            model = ful,
            placeholder = rememberVectorPainter(TrashEmpty),
            fallback = rememberVectorPainter(TrashEmpty),
            error = rememberVectorPainter(TrashEmpty),
            contentDescription = null,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CJText(text = "Annotation:")
            CJText(text = "page: ${(item.annotation.dataPoint + 1).toInt()}")
            if (item.annotation.rowId != null) {
                CJText(text = "rowId: ${item.annotation.rowId}")
            }
        }
    }
}

@Preview
@Composable
fun TimeMachineAnnotationPreview() {
    AppThemePreview {
        val timeMachine = TimeMachine.Annotation(
            annotation = AnnotationDTO(
                id = 1,
                dataPath = "",
                dataPoint = 0.0,
                description = "",
                x = 0f,
                y = 0f,
                width = 0.2f,
                height = 0.2f,
                modifiedTime = nowInMs(),
                rowId = 1
            ),
            modifiedTime = nowInMs()
        )
        Box(Modifier.size(timeMachine.dpSize())) {
            TimeMachineItem(
                modifier = Modifier,
                workspaceFullPath = "",
                item = timeMachine,
                onClick = {},
                fileSize = DpSize(50f.dp, 50f.dp)
            )
        }
    }

}