package com.moly3.cedarjam.core.domain.model.node

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.model.AnnotationId
import com.moly3.cedarjam.core.domain.model.CollectionId
import com.moly3.cedarjam.core.domain.model.RowId
import com.moly3.cedarjam.core.domain.model.TagId
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphPresentation.*
import kotlinx.serialization.Serializable

@Serializable
@Stable
sealed class ObsidianGraphData {
    @Serializable
    data class Tag(val id: TagId) : ObsidianGraphData()

    @Serializable
    data class Collection(val id: CollectionId) : ObsidianGraphData()


    @Serializable
    data class Annotation(val id: AnnotationId, val dataPath: String, val dataPoint: Double) :
        ObsidianGraphData()

    @Serializable
    data class CollectionRow(val id: RowId, val collectionId: CollectionId) : ObsidianGraphData()

    @Serializable
    data class File(
        val relativePath: String,
        val isDirectory: Boolean,
        val extension: String?,
        val fullPath: String
    ) : ObsidianGraphData()
}

sealed class ObsidianGraphPresentation {
    data class Unknown(val value: ObsidianGraphData) : ObsidianGraphPresentation()
    data class Tag(val value: TagDTO) : ObsidianGraphPresentation()
    data class Collection(val value: CollectionDTO) : ObsidianGraphPresentation()
    data class CollectionRow(val value: CollectionRowDTO) : ObsidianGraphPresentation()
    data class File(val value: FileTreeNode) : ObsidianGraphPresentation()
}

fun ObsidianGraphPresentation.toGraphData(): ObsidianGraphData {
    return when (this) {
        is ObsidianGraphPresentation.Collection -> ObsidianGraphData.Collection(this.value.id)
        is ObsidianGraphPresentation.CollectionRow -> ObsidianGraphData.CollectionRow(
            this.value.id,
            collectionId = this.value.collectionId
        )

        is ObsidianGraphPresentation.File -> ObsidianGraphData.File(
            this.value.getRelativePath(),
            isDirectory = this.value.isDirectory(),
            extension = this.value.getExtension(),
            fullPath = this.value.getFullPath()
        )

        is ObsidianGraphPresentation.Tag -> ObsidianGraphData.Tag(this.value.id)
        is ObsidianGraphPresentation.Unknown -> TODO()
    }
}

fun List<ObsidianGraphData>.toPresentation(
    tags: List<TagDTO> = listOf(),
    collections: List<CollectionDTO> = listOf(),
    rows: List<CollectionRowDTO> = listOf(),
    files: List<FileTreeNode> = listOf()
): List<ObsidianGraphPresentation> {
    return this.map {
        when (val data = it) {
            is ObsidianGraphData.Collection -> {
                val collection = collections.firstOrNull { x -> x.id == data.id }
                if (collection != null) {
                    Collection(collection)
                } else {
                    Unknown(data)
                }
            }

            is ObsidianGraphData.CollectionRow -> {
                val row = rows.firstOrNull { x -> x.id == data.id }
                if (row != null) {
                    CollectionRow(row)
                } else {
                    Unknown(data)
                }
            }

            is ObsidianGraphData.File -> {
                val file = files.firstOrNull { x -> x.getRelativePath() == data.relativePath }
                if (file != null) {
                    File(file)
                } else {
                    Unknown(data)
                }
            }

            is ObsidianGraphData.Tag -> {
                val tag = tags.firstOrNull { x -> x.id == data.id }
                if (tag != null) {
                    Tag(tag)
                } else {
                    Unknown(data)
                }
            }

            is ObsidianGraphData.Annotation -> {
                Unknown(data)
            }
        }
    }
}