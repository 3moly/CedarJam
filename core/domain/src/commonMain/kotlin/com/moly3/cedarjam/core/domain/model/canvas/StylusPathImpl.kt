package com.moly3.cedarjam.core.domain.model.canvas

import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.func.ComposeColorSerializer
import com.moly3.dataviz.core.whiteboard.model.StylusPath
import kotlinx.serialization.Serializable

@Serializable
data class StylusPathImpl(
    val points: List<StylusPointImpl>,
    @Serializable(with = ComposeColorSerializer::class)
    val color: Color
)

fun StylusPath.toImpl(): StylusPathImpl {
    return StylusPathImpl(
        points = this.points.map {
            it.toImpl()
        },
        color = this.color
    )
}

fun StylusPathImpl.toSimple(): StylusPath {
    return StylusPath(
        points = this.points.map {
            it.toSimple()
        },
        color = this.color
    )
}