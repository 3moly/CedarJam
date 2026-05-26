package com.moly3.cedarjam.core.domain.repository

import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileStructure
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getHiddenNodes
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.TagAnnotationDTO
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagToTagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.config.GraphSaveConfig
import com.moly3.cedarjam.core.domain.model.config.GraphSaveConfigs
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagAnnotationRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagLinkRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateRowsByPdf
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

interface IWorkspaceEnvironment {

    fun getGraphConfigs(): Flow<List<GraphSaveConfig>>
    suspend fun insertNewGraphConfigs(config: GraphSaveConfigs)
    fun getWorkspaceSettingsFlow(): StateFlow<WorkspaceSettings>

    suspend fun initConfigAndFiles()
    suspend fun setWorkspaceSettings(settings: WorkspaceSettings)
    suspend fun updateTimes()
    suspend fun reinitDatabase()
    suspend fun uploadSync(
        archiveNode: FileTreeNode.File,
        metadata: List<FileMetadata>,
        filesToDownload: List<String>,
        onDownload: suspend (Long, Long?) -> Unit,
        onUpload: suspend (Long, Long?) -> Unit
    ): ResultWrapper<ByteArray, String>

    fun getDatabaseStatus(): Flow<UIState<Unit, DatabaseError>>
    fun getFileNodesFlow(): Flow<UIState<List<FileTreeNode>, String>>
    suspend fun getServerFiles(): ResultWrapper<FileStructure, String>
    suspend fun deleteWorkspaceInServer(): ResultWrapper<Unit, String>
    fun getTagsFlow(): Flow<List<TagDTO>>
    fun getTagFlow(id: Long): Flow<TagDTO?>
    fun getTagFilesFlow(): Flow<List<TagLinkDTO>>
    fun getTagAnnotationsFlow(): Flow<List<TagAnnotationDTO>>
    fun getTagToTagsFlow(): Flow<List<TagToTagDTO>>
    fun getTagCollectionRowsFlow(): Flow<List<TagCollectionRowDTO>>
    fun getCollectionsFlow(): Flow<List<CollectionDTO>>
    fun getCollectionFlow(collectionId: Long): Flow<CollectionDTO?>
    fun getCollectionRowsFlow(collectionId: Long?): Flow<List<CollectionRowDTO>>
    fun getCollectionRowsFlowByFileRelativePath(relativePath: String): Flow<List<CollectionRowDTO>>
    fun getCollectionRowFlow(rowId: Long): Flow<CollectionRowDTO?>
    fun getIndexFilesFlow(): Flow<List<IndexFileDto>>
    fun getIndexFiles(): List<IndexFileDto>
    fun getCollectionRowsCount(collectionId: Long?): Flow<Long>
    fun getCollectionRowsPaginated(
        offset: Long,
        pageSize: Long,
        collectionId: Long
    ): Flow<List<CollectionRowDTO>>

    fun getAnnotationsFlow(): Flow<List<AnnotationDTO>>

    fun isWorkspaceExists(): Boolean
    fun getWorkspace(): WorkspacePresentation

    fun getNodes(absolutePath: String?): List<FileTreeNode>
    suspend fun createFileNode(
        parentRelativePath: String,
        fileName: FileName,
        isAbsoluteNew: Boolean,
        byteArray: ByteArray? = null
    ): ResultWrapper<FileTreeNode.File, String>

    suspend fun createDirectory(
        parentFolder: FileTreeNode.Directory?,
        name: String,
        isAbsoluteNew: Boolean
    ): ResultWrapper<FileTreeNode.Directory, String>

    fun updateIndexFiles(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>

    fun closeDatabase()

    fun updateIndexFilesLocal(
        localNodes: List<FileTreeNode>
    ): ResultWrapper<Unit, String>

    fun syncAllIndexes(specificIndexes: List<IndexFileDto> = listOf()): ResultWrapper<Unit, String>

    fun syncDirtyFiles(list: List<IndexFileDto>): ResultWrapper<Unit, String>
    fun deleteIndexFiles(list: List<String>): ResultWrapper<Unit, String>

    fun setFilesAsSynced(
        paths: List<String>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>


    fun getNodeText(node: FileTreeNode.File): ResultWrapper<String, String>
    suspend fun setNodeText(node: FileTreeNode.File, text: String): ResultWrapper<Unit, String>
    suspend fun createAnnotation(data: CreateAnnotationRequest): ResultWrapper<Long, String>
    fun createTag(request: CreateTagRequest): ResultWrapper<Long, String>
    fun createTagAnnotation(request: CreateTagAnnotationRequest): ResultWrapper<Long, String>
    fun createTagToTag(request: CreateTagToTagRequest): ResultWrapper<Long, String>
    fun createCollection(request: CreateCollectionRequest): ResultWrapper<Long, String>
    fun createCollectionRow(request: CreateCollectionRowRequest): ResultWrapper<Long, String>
    fun createTagLink(request: CreateTagLinkRequest)
    fun createTagCollectionRow(request: CreateTagCollectionRowRequest)
    suspend fun copyFile(
        newFile: FileTreeNode.File,
        byteArray: ByteArray?
    )

    fun updateTag(request: UpdateTagRequest): ResultWrapper<Unit, String>
    fun updateCollection(request: UpdateDataCollectionRequest)
    fun updateCollectionRow(request: UpdateDataCollectionRowRequest)
    fun updateRowsForPdf(request: UpdateRowsByPdf)
    suspend fun renameNode(
        oldNode: FileTreeNode,
        newNode: FileTreeNode
    ): ResultWrapper<FileTreeNode, String>

    fun renameCollection(request: RenameDataCollectionRequest)
    fun renameTag(request: RenameTagRequest)
    fun renameCollectionRow(request: RenameDataCollectionRowRequest)

    fun deleteCollectionRow(id: Long)
    fun deleteCollection(id: Long)
    suspend fun deleteNode(node: FileTreeNode)
    fun deleteTag(id: Long)
    fun deleteTagLink(id: Long)
    fun deleteAnnotation(id: Long)
    fun deleteTagToTag(id: Long)
    fun deleteTagAnnotation(id: Long)
    fun deleteTagCollectionRow(id: Long)
    suspend fun createDatabase()
    suspend fun createDatabaseFiles()
    suspend fun createIndexDatabaseFiles()
    suspend fun saveDeletedMetadata(list: Map<String, IndexFileDto>): ResultWrapper<Unit, String>
    fun getDeletedFilesMetadata(): Map<String, IndexFileDto>

    companion object Companion {
        const val hiddenResources = "resources"

        fun getHiddenDirectory(
            workspace: WorkspacePresentation,
            name: String,
            directoryName: String,
            files: List<FileTreeNode>
        ): List<FileTreeNode> {
            val path = pathWrapper(hiddenDirectory, directoryName)
            val child =
                files.first().getChildrenOrNull()?.getHiddenNodes(directoryName = directoryName)
                    ?: listOf()
            return listOf(
                FileTreeNode.Directory(
                    name = name,
                    workspaceFullPath = workspace.absolutePath,
                    parentRelativePath = path.toString(),
                    children = child,
                    fileSize = child.sumOf { d -> d.fileSize }
                )
            )
        }
    }
}

suspend inline fun <reified T> IWorkspaceEnvironment.setNodeJson(
    node: FileTreeNode.File,
    data: T
): ResultWrapper<Unit, String> {
    val json = DefaultJson.encodeToString(data)
    return setNodeText(node, json)
}

suspend fun IWorkspaceEnvironment.getTags(): List<TagDTO> {
    return getTagsFlow().first()
}

suspend fun IWorkspaceEnvironment.getCollections(): List<CollectionDTO> {
    return getCollectionsFlow().first()
}

suspend fun IWorkspaceEnvironment.getCollectionRows(collectionId: Long?): List<CollectionRowDTO> {
    return getCollectionRowsFlow(collectionId = collectionId).first()
}

fun IWorkspaceEnvironment.getTempFileNode(fileName: FileName): FileTreeNode.File {
    val workspaceAbsolutePath = getWorkspace().absolutePath
    val hiddenDirPath = pathWrapper(hiddenDirectory).pathString
    val importNode =
        FileTreeNode.File(
            fileName,
            workspaceAbsolutePath,
            hiddenDirPath
        )
    return importNode
}