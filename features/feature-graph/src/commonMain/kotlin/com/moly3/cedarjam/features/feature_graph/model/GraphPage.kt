package com.moly3.cedarjam.features.feature_graph.model

import kotlinx.serialization.Serializable

@Serializable
sealed class GraphPage {
    @Serializable
    data object General : GraphPage()

    @Serializable
    data object Tags : GraphPage()

    @Serializable
    data object Annotations : GraphPage()

    @Serializable
    data object Graph : GraphPage()
}