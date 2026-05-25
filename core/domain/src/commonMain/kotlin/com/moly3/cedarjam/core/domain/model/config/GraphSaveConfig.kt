package com.moly3.cedarjam.core.domain.model.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.func.ComposeColorSerializer
import com.moly3.cedarjam.core.domain.model.node.GraphFilter
import com.moly3.dataviz.core.graph.model.GraphSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class GraphSaveConfig(
    @SerialName("is_pinned")
    val isPinned: Boolean,
    @SerialName("name")
    val name: String,
    @SerialName("part")
    val part: GraphPartConfig
)

@Serializable
@Stable
data class GraphPartConfig(
    @SerialName("isStopMoving")
    val isStopMoving: Boolean,
    @SerialName("config")
    val config: GraphSettings,
    @SerialName("settings")
    val filter: GraphFilter,
    @SerialName("groups")
    val groups: List<GroupLogic>
) {
    companion object {
        val Default = GraphPartConfig(
            config = GraphSettings.Default,
            filter = GraphFilter.Default,
            groups = listOf(),
            isStopMoving = false
        )
    }
}

@Serializable
data class GroupLogic(
    @SerialName("is_visible")
    val isVisible: Boolean,
    @SerialName("is_land")
    val isLand: Boolean,
    @SerialName("name")
    val name: String,
    @SerialName("filter")
    val filter: String,
    @Serializable(with = ComposeColorSerializer::class)
    @SerialName("color")
    val color: Color
)