package com.moly3.cedarjam.core.domain.model.request

data class CreateCollectionRowRequest(
    val name: String,
    val collectionId: Long,
    val fileRelativePath: String? = null,
    val imgRelativePath: String? = null,
    val webLink: String? = null,
    val currentProgress: Double? = null,
    val progressMax: Double? = null,
    val isCompleted: Boolean = false,

    val translation: String? = null,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,

    val createdTime: Long
)