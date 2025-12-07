package com.moly3.cedarjam.core.ui.model

import com.moly3.cedarjam.core.domain.model.FileTreeNode

data class PageNameData(
    val name: CJText,
    val pageType: PageType,
    val modifiedTime: Long?
) {
    fun isEditEnabled(): Boolean {
        return when (pageType) {
            is PageType.Collection,
            is PageType.CollectionRow,
            is PageType.FileNode,
            is PageType.Tag -> true

            PageType.Graph,
            PageType.Home,
            PageType.Tags -> false
        }
    }

    sealed class PageType {
        data class Collection(val id: Long) : PageType()
        data class CollectionRow(val id: Long) : PageType()
        data class Tag(val id: Long) : PageType()
        data class FileNode(
            val timestamp: Long,
            val fileTreeNode: FileTreeNode
        ) : PageType()

        data object Graph : PageType()
        data object Tags : PageType()
        data object Home : PageType()
    }
}