package com.moly3.cedarjam.core.storage

import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.db.Annotation
import com.moly3.cedarjam.db.DataCollection
import com.moly3.cedarjam.db.DataCollectionRow
import com.moly3.cedarjam.db.Tag
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
    fun getTagFlow(id: Long): Flow<Tag?>

    fun getAnnotationsFlow(): Flow<List<Annotation>>
    fun createAnnotation(annotation: Annotation): ResultWrapper<Long, String>
    fun getTagLinks(): Flow<List<TagFileNode>>
    fun getCollections(): Flow<List<DataCollection>>
    fun getCollection(id: Long): Flow<DataCollection?>
    fun getTagCollectionRows(): Flow<List<TagCollectionRow>>
    fun getCollectionRows(collectionId: Long?): Flow<List<DataCollectionRow>>
    fun getCollectionRowsCount(collectionId: Long?): Flow<Long>
    fun getCollectionRowsPaginated(
        offset: Long,
        pageSize: Long,
        collectionId: Long
    ): Flow<List<DataCollectionRow>>

    fun getCollectionRow(rowId: Long): Flow<DataCollectionRow?>

    fun createTagToTag(request: CreateTagToTagRequest): ResultWrapper<Long, String>
    fun createTag(tag: Tag): ResultWrapper<Long, String>
    fun updateTag(request: UpdateTagRequest): ResultWrapper<Unit, String>
    fun addTagLink(relativePath: String, tagId: Long)
    fun createTagCollectionRow(request: CreateTagCollectionRowRequest)

    fun updateIndexFiles(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>

    fun updateIndexFilesLocal(
        localNodes: List<FileTreeNode>
    ): ResultWrapper<Unit, String>

    fun syncAllFiles(): ResultWrapper<Unit, String>


    fun syncDirtyFiles(list: List<IndexFileDto>): ResultWrapper<Unit, String>

    fun deleteIndexFiles(list: List<String>): ResultWrapper<Unit, String>
    fun syncIndexDeletedFiles(list: Map<String, IndexFileDto>): ResultWrapper<Unit, String>

    fun setFilesAsSynced(
        paths: List<String>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>

    fun createCollection(request: CreateCollectionRequest): ResultWrapper<Long, String>
    fun createCollectionRow(request: CreateCollectionRowRequest): ResultWrapper<Long, String>

    fun renameCollection(request: RenameDataCollectionRequest)
    fun renameTag(request: RenameTagRequest)
    fun renameFileNode(oldRelativePath: String, newRelativePath: String)
    fun renameCollectionRow(request: RenameDataCollectionRowRequest)


    fun updateCollectionRow(request: UpdateDataCollectionRowRequest)
    fun updateCollection(request: UpdateDataCollectionRequest)

    fun deleteAnnotation(id: Long)
    fun deleteTag(id: Long)
    fun deleteTagLink(id: Long)
    fun deleteCollectionRow(id: Long)
    fun deleteCollection(id: Long)
    fun deleteTagToTag(id: Long)
    fun deleteTagCollectionRow(id: Long)
}