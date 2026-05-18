package com.moly3.cedarjam.core.domain.model.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GraphSaveConfigs(
    @SerialName("configs")
    val configs: List<GraphSaveConfig>,
)
