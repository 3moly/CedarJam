package com.moly3.cedarjam.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class FileType {

    @Serializable
    data class Canvas(val fileNode: FileTreeNode.File) : FileType()
    @Serializable
    data class Text(val value: String) : FileType()
    @Serializable
    data class PDF(val fileNode: FileTreeNode.File, val currentPage: Int) : FileType()
    @Serializable
    data class MIDI(val fileNode: FileTreeNode.File) : FileType()
    @Serializable
    data class Image(val fileNode: FileTreeNode.File) : FileType()
    @Serializable
    data class Video(val fileNode: FileTreeNode.File) : FileType()
    @Serializable
    data object Unknown : FileType()
}