package com.moly3.cedarjam.features.feature_graph.model

import com.moly3.cedarjam.core.domain.model.getCollectionRowGraphId
import com.moly3.cedarjam.core.domain.model.getTagGraphId
import kotlinx.serialization.Serializable

@Serializable
data class DialogConfig(
    val target: GraphDialog?
)

@Serializable
sealed class GraphDialog {
    @Serializable
    data class Tag(val id: Long) : GraphDialog()

    @Serializable
    data class Row(val id: Long) : GraphDialog()

    @Serializable
    data class File(val timestamp: Long) : GraphDialog()
}

fun GraphDialog.toGraphId(): String {
    return when (this) {
        is GraphDialog.Tag -> this.id.getTagGraphId()
        is GraphDialog.Row -> this.id.getCollectionRowGraphId()
        is GraphDialog.File -> "TODO" //TODO
    }
}