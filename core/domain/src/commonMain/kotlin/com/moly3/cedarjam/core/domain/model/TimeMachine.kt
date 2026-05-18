package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

@Stable
sealed class TimeMachine {
    abstract val modifiedTime: Long

    @Stable
    data class FileNode(
        val file: FileTreeNode.File,
        val matches: ImmutableList<LineMatch>?,
        override val modifiedTime: Long
    ) : TimeMachine()

    @Stable
    data class Tag(
        val tag: TagDTO,
        override val modifiedTime: Long
    ) : TimeMachine()

    @Stable
    data class Row(
        val row: CollectionRowDTO,
        override val modifiedTime: Long
    ) : TimeMachine()

    @Stable
    data class Collection(
        val collection: CollectionDTO,
        override val modifiedTime: Long
    ) : TimeMachine()

    @Stable
    data class Annotation(
        val annotation: AnnotationDTO,
        override val modifiedTime: Long
    ) : TimeMachine()
}