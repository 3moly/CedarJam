package com.moly3.cedarjam.core.domain.model

data class TagLinkDTO(
    val id: Long,
    val tagId: Long,
    val data: TagLinkDtoData
)

sealed class TagLinkDtoData {
    data class FileNode(
        val relativePath: String
    ) : TagLinkDtoData()
}