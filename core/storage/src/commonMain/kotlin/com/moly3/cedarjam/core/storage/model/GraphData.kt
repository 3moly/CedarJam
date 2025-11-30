package com.moly3.cedarjam.core.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GraphData(
    @SerialName("version")
    val version: Int,
    @SerialName("shapes")
    val shapes: List<JsonObject>,
    @SerialName("connections")
    val connections: List<JsonObject>
)