package com.moly3.cedarjam.features.feature_graph.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DialogConfig(
    val target: GraphDialogInput
)

@Serializable
sealed class GraphDialogInput {
    @Serializable
    @SerialName("tag")
    data class Tag(val id: Long) : GraphDialogInput()

    @Serializable
    @SerialName("row")
    data class Row(val id: Long) : GraphDialogInput()

    @Serializable
    @SerialName("file")
    data class File(val timestamp: Long) : GraphDialogInput()
}