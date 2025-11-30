package com.moly3.cedarjam.core.storage

import co.touchlab.kermit.Logger
import com.moly3.data.Annotation
import com.moly3.data.DataCollection
import com.moly3.data.DataCollectionRow
import com.moly3.data.Tag
import com.moly3.data.TagCollectionRow
import com.moly3.data.TagFileNode
import com.moly3.data.TagToTag
import com.moly3.cedarjam.core.domain.func.toHexString
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.ensure
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
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class DemoStorageImpl(
    private val applicationProvider: AppContextProvider,
    private val workspaceDirectoryPath: String
) : ISqlStorage {

    private fun nowInMs(): Long = Clock.System.now().toEpochMilliseconds()

    // StateFlows instead of raw lists
    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    private val _tagToTags = MutableStateFlow<List<TagToTag>>(emptyList())
    private val _tagFileNodes = MutableStateFlow<List<TagFileNode>>(emptyList())
    private val _dataCollections = MutableStateFlow<List<DataCollection>>(emptyList())
    private val _collectionRows = MutableStateFlow<List<DataCollectionRow>>(emptyList())
    private val _tagCollectionRows = MutableStateFlow<List<TagCollectionRow>>(emptyList())
    private val _annotations = MutableStateFlow<List<Annotation>>(emptyList())

    private var tagIdCounter = 1L
    private var collectionIdCounter = 1L
    private var rowIdCounter = 1L
    private var tagToTagIdCounter = 1L
    private var tagFileNodeIdCounter = 1L
    private var tagCollectionRowIdCounter = 1L
    private var annotationIdCounter = 1L

    private val _dbFlow = MutableStateFlow<UIState<Unit, DatabaseError>>(UIState.Success(Unit))
    override fun getDatabaseStatus(): Flow<UIState<Unit, DatabaseError>> = _dbFlow

    override fun init() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            Logger.d("Demo storage initialized")
            try {
                _dbFlow.emit(UIState.Success(Unit))
            } catch (exc: Exception) {
                _dbFlow.emit(UIState.Error(DatabaseError.WrongFile(exc.message ?: "")))
            }
        }
    }

    override suspend fun createDatabase() {

    }

    // --- Exposed flows ---
    override fun getTagToTagsFlow(): Flow<List<TagToTag>> = _tagToTags
    override fun getTagsFlow(): Flow<List<Tag>> = _tags
    override fun getTagFlow(id: Long): Flow<Tag?> = _tags.map { it.find { t -> t.id == id } }
    override fun getTagLinks(): Flow<List<TagFileNode>> = _tagFileNodes
    override fun getCollections(): Flow<List<DataCollection>> = _dataCollections
    override fun getCollection(id: Long): Flow<DataCollection?> =
        _dataCollections.map { it.find { c -> c.id == id } }

    override fun getTagCollectionRows(): Flow<List<TagCollectionRow>> = _tagCollectionRows

    override fun getCollectionRows(collectionId: Long?): Flow<List<DataCollectionRow>> =
        _collectionRows.map { rows ->
            if (collectionId == null) rows else rows.filter { it.collectionId == collectionId }
        }

    override fun getCollectionRowsCount(collectionId: Long?): Flow<Long> =
        _collectionRows.map { rows -> rows.count { it.collectionId == collectionId }.toLong() }

    override fun getCollectionRowsPaginated(
        offset: Long,
        pageSize: Long,
        collectionId: Long
    ): Flow<List<DataCollectionRow>> =
        _collectionRows.map { rows ->
            rows.filter { it.collectionId == collectionId }
                .drop(offset.toInt())
                .take(pageSize.toInt())
        }

    override fun getCollectionRow(rowId: Long): Flow<DataCollectionRow?> =
        _collectionRows.map { rows -> rows.find { it.id == rowId } }

    // --- Create ---
    override fun createCollection(request: CreateCollectionRequest): ResultWrapper<Long, String> {
        val id = collectionIdCounter++
        val newCollection = DataCollection(
            id,
            request.name,
            0,
            request.createdTime,
            request.createdTime
        )
        _dataCollections.value = _dataCollections.value + newCollection
        return ResultWrapper.Success(id)
    }

    override fun createCollectionRow(request: CreateCollectionRowRequest): ResultWrapper<Long, String> {
        if (request.name.isEmpty()) throw NullPointerException("collection row - name is empty")
        if (_collectionRows.value.any { it.name == request.name && it.collectionId == request.collectionId })
            throw NullPointerException("row with this name exists")

        val id = rowIdCounter++
        val row = DataCollectionRow(
            id = id,
            name = request.name,
            collectionId = request.collectionId,
            createdTime = request.createdTime,
            modifiedTime = request.createdTime,
            currentProgress = request.currentProgress,
            progressMax = request.progressMax,
            webLink = request.webLink,
            fileRelativePath = request.fileRelativePath,
            imgRelativePath = request.imgRelativePath,
            translation = request.translation,
            exampleSentence = request.exampleSentence,
            pronunciation = request.pronunciation,
            isCompleted = if (request.isCompleted) 1 else 0
        )
        _collectionRows.value = _collectionRows.value + row
        return ResultWrapper.Success(id)
    }

    override fun createTag(tag: Tag): ResultWrapper<Long, String> {
        return resultBlock {
            ensure(!_tags.value.any { it.name == tag.name }) { "tag with this name is already exists" }
            _tags.value = _tags.value + tag.copy(id = tagIdCounter++)
            tagIdCounter
        }
    }

    override fun createTagToTag(request: CreateTagToTagRequest): ResultWrapper<Long, String> {
        if (_tagToTags.value.any { it.firstTagId == request.tagId && it.secondTagId == request.tag2Id })
            return ResultWrapper.Error("")
        val newTagToTag = TagToTag(
            tagToTagIdCounter++,
            request.tagId,
            request.tag2Id,
            request.modifiedTime
        )
        _tagToTags.value = _tagToTags.value + newTagToTag
        return ResultWrapper.Success(newTagToTag.id)
    }

    override fun addTagLink(relativePath: String, tagId: Long) {
        val link = TagFileNode(
            id = tagFileNodeIdCounter++,
            tagId = tagId,
            fileNodeRelativePath = relativePath,
            createdTime = nowInMs()
        )
        _tagFileNodes.value = _tagFileNodes.value + link
    }

    override fun createTagCollectionRow(request: CreateTagCollectionRowRequest) {
        val row = TagCollectionRow(
            tagCollectionRowIdCounter++,
            request.tagId,
            request.rowId,
            request.createdTime
        )
        _tagCollectionRows.value = _tagCollectionRows.value + row
    }

    override fun insertOrUpdateFileNodes(workspaceFullPath: String, data: List<FileTreeNode>) {
        // demo only – do nothing
    }

    // --- Updates ---
    override fun renameTag(request: RenameTagRequest) {
        if (_tags.value.any { it.name == request.newName }) return
        _tags.value = _tags.value.map {
            if (it.id == request.id)
                it.copy(name = request.newName, modifiedTime = request.modifiedTime)
            else it
        }
    }

    override fun updateTag(request: UpdateTagRequest): ResultWrapper<Unit, String> {
        return resultBlock {
            _tags.value = _tags.value.map {
                if (it.id == request.id) {
                    it.copy(
                        color = request.color.toHexString(),
                        modifiedTime = request.modifiedTime
                    )
                } else it
            }
        }
    }

    override fun renameFileNode(oldRelativePath: String, newRelativePath: String) {
        // implement if needed
    }

    override fun renameCollectionRow(request: RenameDataCollectionRowRequest) {
        if (request.newName.isEmpty()) return
        _collectionRows.value = _collectionRows.value.map {
            if (it.id == request.rowId) it.copy(
                name = request.newName,
                modifiedTime = request.modifiedTime
            ) else it
        }
    }

    override fun renameCollection(request: RenameDataCollectionRequest) {
        _dataCollections.value = _dataCollections.value.map {
            if (it.id == request.id) it.copy(
                name = request.newName,
                modifiedTime = request.modifiedTime
            ) else it
        }
    }

    override fun updateCollection(request: UpdateDataCollectionRequest) {
        _dataCollections.value = _dataCollections.value.map {
            if (it.id == request.id) it.copy(
                viewType = request.viewType.num,
                modifiedTime = request.modifiedTime
            ) else it
        }
    }

    override fun updateCollectionRow(request: UpdateDataCollectionRowRequest) {
        _collectionRows.value = _collectionRows.value.map {
            if (it.id == request.id) {
                it.copy(
                    currentProgress = request.currentProgress,
                    progressMax = request.progressMax,
                    webLink = request.webLink,
                    fileRelativePath = request.fileRelativePath,
                    imgRelativePath = request.imgRelativePath,
                    isCompleted = if (request.isCompleted) 1 else 0,
                    translation = request.translation,
                    exampleSentence = request.exampleSentence,
                    pronunciation = request.pronunciation,
                    modifiedTime = request.modifiedTime
                )
            } else it
        }
    }

    // --- Deletes ---
    override fun deleteTag(id: Long) {
        _tags.value = _tags.value.filterNot { it.id == id }
    }

    override fun deleteTagLink(id: Long) {
        _tagFileNodes.value = _tagFileNodes.value.filterNot { it.id == id }
    }

    override fun deleteCollectionRow(id: Long) {
        _collectionRows.value = _collectionRows.value.filterNot { it.id == id }
    }

    override fun deleteCollection(id: Long) {
        _dataCollections.value = _dataCollections.value.filterNot { it.id == id }
        _collectionRows.value = _collectionRows.value.filterNot { it.collectionId == id }
    }

    override fun deleteAnnotation(id: Long) {
        //_annotations.value = _annotations.value.filterNot { it.id == id }
    }

    override fun deleteTagToTag(id: Long) {
        _tagToTags.value = _tagToTags.value.filterNot { it.id == id }
    }

    override fun deleteTagCollectionRow(id: Long) {
        _tagCollectionRows.value = _tagCollectionRows.value.filterNot { it.id == id }
    }
}