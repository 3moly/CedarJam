package com.moly3.cedarjam.core.domain.model.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.func.ComposeColorSerializer
import com.moly3.dataviz.core.whiteboard.func.PathBounds
import com.moly3.dataviz.core.whiteboard.model.StylusPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ShapeData {
    @Serializable
    @SerialName("Text")
    data class Text(val text: String) : ShapeData()

    @Serializable
    @SerialName("FileNode")
    data class FileNode(val relativeToFilePath: String) : ShapeData()

    @Serializable
    @SerialName("Drawing")
    data class Drawing(val value: MyStylusPath) : ShapeData()
}

@Serializable
data class MyStylusPath(
    val points: List<MyStylusPoint>,
    @Serializable(with = ComposeColorSerializer::class)
    val color: Color
)


@Serializable
data class MyStylusPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val tiltX: Float = 0f,
    val tiltY: Float = 0f,
    val strokeWidth: Float = 5f,
    val timestamp: Long
)

fun MyStylusPath.calculateBounds(): PathBounds {
    if (points.isEmpty()) {
        return PathBounds(androidx.compose.ui.geometry.Size.Zero, Offset.Zero, Offset.Zero)
    }

    var minX = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE // Fix: Use negative MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxY = -Float.MAX_VALUE // Fix: Use negative MAX_VALUE

    for (point in points) {
        val strokeRadius = point.strokeWidth / 2f

        val leftEdge = point.x - strokeRadius
        val rightEdge = point.x + strokeRadius
        val topEdge = point.y - strokeRadius
        val bottomEdge = point.y + strokeRadius

        if (leftEdge < minX) minX = leftEdge
        if (rightEdge > maxX) maxX = rightEdge
        if (topEdge < minY) minY = topEdge
        if (bottomEdge > maxY) maxY = bottomEdge
    }

    val width = maxX - minX
    val height = maxY - minY

    val size = Size(width, height)
    val localCenter = Offset(width / 2f, height / 2f)
    val globalPosition = Offset(minX, minY)

    return PathBounds(size, localCenter, globalPosition)
}