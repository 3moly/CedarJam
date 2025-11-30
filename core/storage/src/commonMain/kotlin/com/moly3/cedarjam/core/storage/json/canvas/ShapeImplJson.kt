package com.moly3.cedarjam.core.storage.json.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.model.canvas.ShapeData
import com.moly3.cedarjam.core.domain.func.ComposeColorSerializer
import com.moly3.cedarjam.core.domain.func.ComposeOffsetSerializer
import com.moly3.dataviz.core.block.model.Shape
import kotlinx.serialization.Serializable

@Serializable
data class ShapeImplJson(
    override val id: Long,
    @Serializable(with = ComposeOffsetSerializer::class)
    override val position: Offset,
    @Serializable(with = ComposeOffsetSerializer::class)
    override val size: Offset,
    @Serializable(with = ComposeColorSerializer::class)
    val color: Color? = null,
    val fileData: ShapeData
) : Shape
