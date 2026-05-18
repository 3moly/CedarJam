package com.moly3.cedarjam.core.domain.model.node

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class GraphSettingsConfig(
    val maxNodes: Int,
    val isShowDirectories: Boolean,
    val isRealFiles: Boolean,
    val isTags: Boolean,
    val isRows: Boolean,
    val isAnnotations: Boolean,
    val isCollections: Boolean,
    val isOrphans: Boolean,
    val isGradations: Boolean,
    val isShowImages: Boolean,

) {
    companion object {
        val Default = GraphSettingsConfig(
            isTags = true,
            isCollections = true,
            isRows = true,
            isAnnotations = true,

            isRealFiles = false,
            isShowDirectories = true,

            isOrphans = true,
            maxNodes = 30_000,
            isGradations = false,
            isShowImages = false,
        )
    }
}