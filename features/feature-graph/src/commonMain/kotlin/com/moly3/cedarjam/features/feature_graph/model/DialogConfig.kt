package com.moly3.cedarjam.features.feature_graph.model

import kotlinx.serialization.Serializable

@Serializable
data class DialogConfig(
    val targetId: String?
)