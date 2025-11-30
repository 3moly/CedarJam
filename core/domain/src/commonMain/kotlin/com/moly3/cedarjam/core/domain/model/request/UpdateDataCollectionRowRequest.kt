package com.moly3.cedarjam.core.domain.model.request

import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO

data class UpdateDataCollectionRowRequest(
    val id: Long,
    val currentProgress: Double?,
    val progressMax: Double?,
    val webLink: String?,
    val fileRelativePath: String?,
    val imgRelativePath: String?,
    val isCompleted: Boolean,

    val translation: String?,
    val exampleSentence: String?,
    val pronunciation: String?,

    val modifiedTime: Long
)

fun CollectionRowDTO.mapToUpdateRequest(): UpdateDataCollectionRowRequest {
    return UpdateDataCollectionRowRequest(
        id = this.id,
        currentProgress = this.currentProgress,
        progressMax = this.progressMax,
        webLink = this.webLink,

        fileRelativePath = this.fileRelativePath?.normalizeText(),
        imgRelativePath = this.imgRelativePath?.normalizeText(),

        isCompleted = this.isCompleted,
        translation = this.translation,
        exampleSentence = this.exampleSentence,
        pronunciation = this.pronunciation,
        modifiedTime = this.modifiedTime,
    )
}