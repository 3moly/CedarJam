package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class CollectionRowDTO(
    val id: RowId,
    val name: String,
    val collectionId: CollectionId,

    val fileRelativePath: String? = null,
    val imgRelativePath: String? = null,
    val webLink: String? = null,
    val currentProgress: Double? = null,
    val progressMax: Double? = null,
    val isCompleted: Boolean = false,

    val translation: String? = null,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,

    val createdTime: Long,
    val modifiedTime: Long,

    val points: Long
)