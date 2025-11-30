package com.moly3.cedarjam.core.ui.model

import androidx.compose.ui.graphics.Color
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.TagDTO
import kotlinx.collections.immutable.ImmutableList

data class FileTreeItemPresentation(
    val key: String,
    val name: String,
    val backColor: Color? = null,
    val fileExtension: String? = null,
    val data: FileTreeItemPresentationData,
    val children: ImmutableList<FileTreeItemPresentation>? = null
) {
    sealed class FileTreeItemPresentationData {
        data object Home : FileTreeItemPresentationData()
        data object Graph : FileTreeItemPresentationData()
        data object Tags : FileTreeItemPresentationData()
        data object Collections : FileTreeItemPresentationData()
        data class Directory(val fileNode: FileTreeNode.Directory, val isDragEnabled: Boolean) :
            FileTreeItemPresentationData()

        data class File(val fileNode: FileTreeNode.File) : FileTreeItemPresentationData()
        data class Collection(val id: Long) : FileTreeItemPresentationData()
        data class Tag(val tag: TagDTO) : FileTreeItemPresentationData()
    }
}