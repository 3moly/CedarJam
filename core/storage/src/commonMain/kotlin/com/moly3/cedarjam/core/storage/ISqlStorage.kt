package com.moly3.cedarjam.core.storage

import com.moly3.cedarjam.core.domain.model.AnnotationId
import com.moly3.cedarjam.core.domain.model.CollectionId
import com.moly3.cedarjam.core.domain.model.RowId
import com.moly3.cedarjam.core.domain.model.TagAnnotationId
import com.moly3.cedarjam.core.domain.model.TagRowId
import com.moly3.cedarjam.core.domain.model.TagId
import com.moly3.cedarjam.core.domain.model.TagLinkId
import com.moly3.cedarjam.core.domain.model.TagToTagId
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagAnnotationRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateRowsByPdf
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.db.Annotation
import com.moly3.cedarjam.db.DataCollection
import com.moly3.cedarjam.db.DataCollectionRow
import com.moly3.cedarjam.db.Tag
import com.moly3.cedarjam.db.TagAnnotation
import com.moly3.cedarjam.db.TagCollectionRow
import com.moly3.cedarjam.db.TagFileNode
import com.moly3.cedarjam.db.TagToTag
import com.moly3.cedarjam.indexdb.IndexFile

import kotlinx.coroutines.flow.Flow

interface ISqlStorage {
    fun init()
    suspend fun createDbFiles()
    suspend fun createIndexDbFiles()
    suspend fun createDatabase()
    fun getDatabaseStatus(): Flow<UIState<Unit, DatabaseError>>
    fun getIndexFilesFlow(): Flow<List<IndexFile>>
    fun getIndexFiles(): List<IndexFile>
    fun close()

    fun getTagToTagsFlow(): Flow<List<TagToTag>>
    fun getTagsFlow(): Flow<List<Tag>>
    fun getTagFlow(id: TagId): Flow<Tag?>

    fun getAnnotationsFlow(): Flow<List<Annotation>>
    fun createAnnotation(annotation: Annotation): ResultWrapper<AnnotationId, String>
    fun getTagFiles(): Flow<List<TagFileNode>>
    fun getTagAnnotations(): Flow<List<TagAnnotation>>
    fun getCollections(): Flow<List<DataCollection>>
    fun getCollection(id: CollectionId): Flow<DataCollection?>
    fun getTagCollectionRows(): Flow<List<TagCollectionRow>>
    fun getCollectionRows(collectionId: CollectionId?): Flow<List<DataCollectionRow>>
    fun getCollectionRowsByFileRelativePath(relativePath: String): Flow<List<DataCollectionRow>>
    fun getCollectionRowsCount(collectionId: CollectionId?): Flow<Long>
    fun getCollectionRowsPaginated(
        offset: Long,
        pageSize: Long,
        collectionId: CollectionId
    ): Flow<List<DataCollectionRow>>

    fun getCollectionRow(rowId: RowId): Flow<DataCollectionRow?>

    fun createTagAnnotation(request: CreateTagAnnotationRequest): ResultWrapper<TagAnnotationId, String>
    fun createTagToTag(request: CreateTagToTagRequest): ResultWrapper<TagToTagId, String>
    fun createTag(tag: Tag): ResultWrapper<TagId, String>
    fun updateTag(request: UpdateTagRequest): ResultWrapper<Unit, String>
    fun addTagLink(relativePath: String, tagId: TagId)
    fun createTagCollectionRow(request: CreateTagCollectionRowRequest): ResultWrapper<TagRowId, String>

    fun updateIndexFiles(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>

    fun updateIndexFilesLocal(
        localNodes: List<FileTreeNode>
    ): ResultWrapper<Unit, String>

    fun syncAllFiles(specificIndexes: List<IndexFileDto>): ResultWrapper<Unit, String>

    fun syncDirtyFiles(list: List<IndexFileDto>): ResultWrapper<Unit, String>

    fun deleteIndexFiles(list: List<String>): ResultWrapper<Unit, String>
    fun syncIndexDeletedFiles(list: Map<String, IndexFileDto>): ResultWrapper<Unit, String>

    fun setFilesAsSynced(
        paths: List<String>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>

    fun createCollection(request: CreateCollectionRequest): ResultWrapper<CollectionId, String>
    fun createRow(request: CreateCollectionRowRequest): ResultWrapper<RowId, String>

    fun renameCollection(request: RenameDataCollectionRequest)
    fun renameTag(request: RenameTagRequest)
    fun renameFileNode(oldRelativePath: String, newRelativePath: String)
    fun renameCollectionRow(request: RenameDataCollectionRowRequest)


    fun updateCollectionRow(request: UpdateDataCollectionRowRequest)
    fun updateRowsForPdf(request: UpdateRowsByPdf)
    fun updateCollection(request: UpdateDataCollectionRequest)

    fun deleteAnnotation(id: AnnotationId)
    fun deleteTag(id: TagId)
    fun deleteTagLink(id: TagLinkId)
    fun deleteCollectionRow(id: RowId)
    fun deleteCollection(id: CollectionId)
    fun deleteTagToTag(id: TagToTagId)
    fun deleteTagCollectionRow(id: TagRowId)
    fun deleteTagAnnotation(id: TagAnnotationId)

}