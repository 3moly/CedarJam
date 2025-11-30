package com.moly3.cedarjam.pages.page_home.model

import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.TagDTO
import kotlinx.collections.immutable.ImmutableList

sealed class TimeMachine {
    abstract val modifiedTime: Long

    data class FileNode(
        val file: FileTreeNode,
        val matches: ImmutableList<LineMatch>?,
        override val modifiedTime: Long
    ) : TimeMachine()

    data class Tag(
        val tag: TagDTO,
        override val modifiedTime: Long
    ) : TimeMachine()

    data class Row(
        val row: CollectionRowDTO,
        override val modifiedTime: Long
    ) : TimeMachine()

    data class Collection(
        val collection: CollectionDTO,
        override val modifiedTime: Long
    ) : TimeMachine()
}