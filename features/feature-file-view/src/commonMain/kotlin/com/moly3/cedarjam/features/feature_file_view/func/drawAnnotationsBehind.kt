package com.moly3.cedarjam.features.feature_file_view.func

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.compose.ZoomState
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.features.feature_file_view.internal.toPx
import kotlinx.collections.immutable.ImmutableList

fun Modifier.drawAnnotationsBehind(
    zoomState: ZoomState,
    currentPage: Int,
    annotations: ImmutableList<AnnotationDTO>
): Modifier {
    val zoomable = zoomState.zoomable

    return this.drawWithContent {
        // 1. Draw the image content first
        this.drawContent()

        // 2. Filter annotations for the current page
        annotations.filter { d -> d.dataPoint.toInt() == currentPage - 1 }
            .forEach { annotation ->

                // Get the original rect based on the content's unscaled size
                val baseRect = annotation.toPx(zoomable.contentSize)

                // 3. Map the content-space Rect to the current display-space Rect
                // This handles scale, offset, and rotation automatically

                val displayRect = zoomable.sourceToDraw(baseRect)

                // 4. Draw the background highlight
                drawRect(
                    color = Color.Yellow.copy(alpha = 0.35f),
                    topLeft = displayRect.topLeft,
                    size = displayRect.size
                )

                // 5. Draw the border
                drawRect(
                    color = Color.Yellow,
                    topLeft = displayRect.topLeft,
                    size = displayRect.size,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
    }
}

fun Modifier.drawAnnotationsBehind(
    currentPage: Int,
    annotations: ImmutableList<AnnotationDTO>
): Modifier {
    return this.drawWithContent {
        this.drawContent()
        val size = this.size

        annotations.filter { d -> d.dataPoint.toInt() == currentPage - 1 }
            .forEach { annotation ->

                val rect = annotation.toPx(
                    IntSize(
                        size.width.toInt(),
                        size.height.toInt()
                    )
                )

                drawRect(
                    color = Color.Yellow.copy(alpha = 0.35f),
                    topLeft = Offset(
                        rect.left,
                        rect.top
                    ),
                    size = Size(
                        rect.width,
                        rect.height
                    )
                )

                drawRect(
                    color = Color.Yellow,
                    topLeft = Offset(
                        rect.left,
                        rect.top
                    ),
                    size = Size(
                        rect.width,
                        rect.height
                    ),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
    }
}