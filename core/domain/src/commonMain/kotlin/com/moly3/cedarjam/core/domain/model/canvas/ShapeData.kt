package com.moly3.cedarjam.core.domain.model.canvas

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
}
