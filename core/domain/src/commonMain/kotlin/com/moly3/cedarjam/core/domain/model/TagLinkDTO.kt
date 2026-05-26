package com.moly3.cedarjam.core.domain.model

data class TagLinkDTO(
    val id: TagLinkId,
    val tagId: TagId,
    val data: TagLinkDtoData
)

sealed class TagLinkDtoData {
    data class FileNode(
        val relativePath: String
    ) : TagLinkDtoData()
}