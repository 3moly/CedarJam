package com.moly3.cedarjam.core.domain.repository

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
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagToTagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagLinkRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

interface IWorkspaceEnvironment {
    fun getWorkspaceSettingsFlow(): StateFlow<WorkspaceSettings>

    suspend fun initConfigAndFiles()
    suspend fun setWorkspaceSettings(settings: WorkspaceSettings)
    suspend fun updateTimes()
    suspend fun reinitDatabase()
    suspend fun uploadSync(
        archiveFullPath: String,
        metadata: List<FileMetadata>,
        filesToDownload: List<String>
    ): ResultWrapper<ByteArray, String>

    fun getDatabaseStatus(): Flow<UIState<Unit, DatabaseError>>
    fun getFileNodesFlow(): Flow<UIState<List<FileTreeNode>, String>>
    suspend fun getServerFiles(): ResultWrapper<FileStructure, String>
    fun getTagsFlow(): Flow<List<TagDTO>>
    fun getTagFlow(id: Long): Flow<TagDTO?>
    fun getTagLinksFlow(): Flow<List<TagLinkDTO>>
    fun getTagToTagsFlow(): Flow<List<TagToTagDTO>>
    fun getTagCollectionRowsFlow(): Flow<List<TagCollectionRowDTO>>
    fun getCollectionsFlow(): Flow<List<CollectionDTO>>
    fun getCollectionFlow(collectionId: Long): Flow<CollectionDTO?>
    fun getCollectionRowsFlow(collectionId: Long?): Flow<List<CollectionRowDTO>>
    fun getCollectionRowFlow(rowId: Long): Flow<CollectionRowDTO?>
    fun getIndexFilesFlow(): Flow<List<IndexFileDto>>
    fun getCollectionRowsCount(collectionId: Long?): Flow<Long>
    fun getCollectionRowsPaginated(
        offset: Long,
        pageSize: Long,
        collectionId: Long
    ): Flow<List<CollectionRowDTO>>

    fun getAnnotationsFlow(): Flow<List<AnnotationDTO>>

    fun isWorkspaceExists(): Boolean
    fun getWorkspace(): WorkspacePresentation

    fun getNodes(parentFolder: FileTreeNode.Directory?): List<FileTreeNode>
    suspend fun createFileNode(
        parentFolder: FileTreeNode.Directory?,
        fileName: FileName,
        isAbsoluteNew: Boolean,
        byteArray: ByteArray? = null
    ): ResultWrapper<FileTreeNode.File, String>

    suspend fun createDirectory(
        parentFolder: FileTreeNode.Directory?,
        name: String,
        isAbsoluteNew: Boolean
    ): ResultWrapper<FileTreeNode.Directory, String>

    fun updateIndexFilesFlow(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>

    fun finishIndexFiles(): ResultWrapper<Unit, String>
    fun deleteIndexFiles(list:List<String>): ResultWrapper<Unit, String>

    fun setFilesAsSynced(
        paths: List<String>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String>



    fun getNodeText(node: FileTreeNode.File): ResultWrapper<String, String>
    suspend fun setNodeText(node: FileTreeNode.File, text: String): ResultWrapper<Unit, String>
    fun createAnnotation(data: AnnotationDTO)
    fun createTag(request: CreateTagRequest): ResultWrapper<Long, String>
    fun createTagToTag(request: CreateTagToTagRequest): ResultWrapper<Long, String>
    fun createCollection(request: CreateCollectionRequest): ResultWrapper<Long, String>
    fun createCollectionRow(request: CreateCollectionRowRequest): ResultWrapper<Long, String>
    fun createTagLink(request: CreateTagLinkRequest)
    fun createTagCollectionRow(request: CreateTagCollectionRowRequest)
    suspend fun copyFile(
        originalFullPath: String,
        newFile: FileTreeNode.File,
        byteArray: ByteArray?
    )

    fun updateTag(request: UpdateTagRequest): ResultWrapper<Unit, String>
    fun updateCollection(request: UpdateDataCollectionRequest)
    fun updateCollectionRow(request: UpdateDataCollectionRowRequest)
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
    suspend fun deleteNodes(nodes: List<FileTreeNode>)
    fun deleteTag(id: Long)
    fun deleteTagLink(id: Long)
    fun deleteAnnotation(id: Long)
    fun deleteTagToTag(id: Long)
    fun deleteTagCollectionRow(id: Long)
    suspend fun createDatabase()
    suspend fun saveDeletedMetadata(list: Map<String, Long>): ResultWrapper<Unit, String>
    fun getDeletedFilesMetadata(): Map<String, Long>

    companion object Companion {
        const val hiddenResources = "resources"

        fun getHiddenDirectory(
            workspace: WorkspacePresentation,
            name: String,
            directoryName: String,
            files: List<FileTreeNode>
        ): List<FileTreeNode> {
            val path = pathWrapper(workspace.fullpath, hiddenDirectory, directoryName)
            val child =
                files.first().getChildrenOrNull()?.getHiddenNodes(directoryName = directoryName)
                    ?: listOf()
            return listOf(
                FileTreeNode.Directory(
                    name = name,
                    //todo adapt relativePath
                    parentRelativePath = path.toString(),
                    parentFullPath = path.toString(),
                    children = child,
                    fileSize = child.sumOf { d -> d.fileSize }
                )
            )
        }
    }
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