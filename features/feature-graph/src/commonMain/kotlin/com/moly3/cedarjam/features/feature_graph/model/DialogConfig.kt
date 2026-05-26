package com.moly3.cedarjam.features.feature_graph.model

import com.moly3.cedarjam.core.domain.model.RowId
import com.moly3.cedarjam.core.domain.model.TagId
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
    data class Tag(val id: TagId) : GraphDialogInput()

    @Serializable
    @SerialName("row")
    data class Row(val id: RowId) : GraphDialogInput()

    @Serializable
    @SerialName("file")
    data class File(val timestamp: Long) : GraphDialogInput()
}