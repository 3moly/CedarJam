package com.moly3.cedarjam.core.data

import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.func.doNothing
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.toColor
import com.moly3.cedarjam.core.domain.func.toHexString
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileStructure
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.domain.model.TagAnnotationDTO
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDtoData
import com.moly3.cedarjam.core.domain.model.TagToTagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.fold
import com.moly3.cedarjam.core.domain.model.getSettingsJsonFile
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.toCollectionViewType
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
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
import com.moly3.cedarjam.core.domain.model.request.UpdateRowsByPdf
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.storage.ISqlStorage
import com.moly3.cedarjam.db.Annotation
import com.moly3.cedarjam.db.DataCollectionRow
import com.moly3.cedarjam.db.Tag
import com.moly3.cedarjam.indexdb.IndexFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.set
import kotlin.time.ExperimentalTime

class WorkspaceEnvironment(
    private val sqlStorageFactory: () -> ISqlStorage,//core:data
    private val workspace: WorkspacePresentation,
    private val filesRepository: IFilesRepository,//core:data
    private val fileManagerService: FileManagerService, //core:data
    private val syncNetRepository: IRemoteSyncRepository, //-> core:net,
) : IWorkspaceEnvironment {

    private val fileNodesState: MutableStateFlow<UIState<List<FileTreeNode>, String>> =
        MutableStateFlow(UIState.Loading)
    private val _appSettingsStateFlow = MutableStateFlow(WorkspaceSettings.defaultSettings)

    private val sqlStorage: ISqlStorage by lazy {
        println("--- workspace sqlstorage init ${workspace.name} ---")
        val sqlStorage = sqlStorageFactory()
        sqlStorage.init()
        sqlStorage
    }

    private fun walkInFiles(
        files: List<FileTreeNode>,
        watch: (FileTreeNode) -> Unit
    ) {
        for (item in files) {
            if (item is FileTreeNode.Directory) {
                walkInFiles(item.children, watch = watch)
            }
            watch(item)
        }
    }

    override suspend fun initConfigAndFiles() {
        withContext(io) {
            updateTimes()
            updateWorkspaceSettings()
        }
    }

    suspend fun updateWorkspaceSettings() {
        val settingsFile = workspace.getSettingsJsonFile()
        val settings = try {
            val jsonResult = filesRepository.getNodeText(settingsFile)
            jsonResult.fold(
                onFailure = {
                    WorkspaceSettings.defaultSettings
                },
                onSuccess = {
                    DefaultJson.decodeFromString<WorkspaceSettings>(it)
                }
            )
        } catch (exc: Exception) {
            WorkspaceSettings.defaultSettings
        }
        _appSettingsStateFlow.emit(settings)
    }


    override fun getWorkspaceSettingsFlow(): StateFlow<WorkspaceSettings> {
        return _appSettingsStateFlow
    }

    override suspend fun setWorkspaceSettings(settings: WorkspaceSettings) {
        withContext(io) {
            val json = DefaultJson.encodeToString(settings)
            filesRepository.setNodeText(workspace.getSettingsJsonFile(), json)
            _appSettingsStateFlow.emit(settings)
        }
    }

    override fun getNodes(absolutePath: String?): List<FileTreeNode> {
        val absolutePathGl = absolutePath ?: workspace.absolutePath
        return filesRepository.getNodes(
            workspacePath = workspace.absolutePath,
            absolutePath = absolutePathGl
        )
    }

    private fun tryToGet(): UIState<List<FileTreeNode>, String> {
        return try {
            val files = getNodes(null)
            UIState.Success(files)
        } catch (exc: Exception) {
            UIState.Error(exc.message ?: "")
        }
    }

    override suspend fun uploadSync(
        archiveNode: FileTreeNode.File,
        metadata: List<FileMetadata>,
        filesToDownload: List<String>,
        onDownload: suspend (Long, Long?) -> Unit,
        onUpload: suspend (Long, Long?) -> Unit,
    ): ResultWrapper<ByteArray, String> {
        return resultBlock {
            var byteArray: ByteArray? = null
            try {
                byteArray = filesRepository.getNodeBytes(archiveNode)
            } catch (exc: Exception) {
            }

            val uploadResult = syncNetRepository.upload(
                userName = "bulat",
                workspaceName = workspace.serverName,
                archiveByteArray = byteArray,
                metadata = metadata,
                filesToDownload = filesToDownload,
                onUpload = onUpload,
                onDownload = onDownload
            )
            bind(uploadResult)
        }
    }

    private val refreshMutex = Mutex()

    override suspend fun updateTimes() {
        withContext(io) {
            if (fileNodesState.value is UIState.Success && false) {

            } else {
                refreshMutex.withLock {
                    val state = tryToGet()
                    fileNodesState.emit(state)
                }
            }
        }
    }

    override suspend fun reinitDatabase() {
        sqlStorage.init()
    }

    override fun getDatabaseStatus(): Flow<UIState<Unit, DatabaseError>> {
        return sqlStorage.getDatabaseStatus()
    }

    override fun getFileNodesFlow(): Flow<UIState<List<FileTreeNode>, String>> {
        return fileNodesState
    }

    override suspend fun getServerFiles(): ResultWrapper<FileStructure, String> {
        return syncNetRepository.workspaceFiles(
            userName = "bulat",
            workspaceName = workspace.serverName
        )
    }

    override suspend fun deleteWorkspaceInServer(): ResultWrapper<Unit, String> {
        return syncNetRepository.deleteWorkspace(
            userName = "bulat",
            workspaceName = workspace.serverName
        )
    }

    override fun getCollectionsFlow(): Flow<List<CollectionDTO>> {
        return sqlStorage
            .getCollections()
            .map {
                it.map { d ->
                    CollectionDTO(
                        id = d.id,
                        name = d.name,
                        viewType = d.viewType.toCollectionViewType(),
                        createdTime = d.createdTime,
                        modifiedTime = d.modifiedTime
                    )
                }
            }
            .flowOn(io)
    }

    override fun getCollectionFlow(collectionId: Long): Flow<CollectionDTO?> {
        return sqlStorage
            .getCollection(id = collectionId)
            .map {
                it?.let { d ->
                    CollectionDTO(
                        id = d.id,
                        name = d.name,
                        viewType = d.viewType.toCollectionViewType(),
                        createdTime = d.createdTime,
                        modifiedTime = d.modifiedTime
                    )
                }
            }
            .flowOn(io)
    }

    internal fun DataCollectionRow.toDTO(): CollectionRowDTO {
        return CollectionRowDTO(
            id = this.id,
            name = this.name,
            collectionId = this.collectionId,
            fileRelativePath = this.fileRelativePath,
            imgRelativePath = this.imgRelativePath,
            webLink = this.webLink,
            currentProgress = this.currentProgress,
            progressMax = this.progressMax,
            isCompleted = this.isCompleted == 1L,
            createdTime = this.createdTime,
            modifiedTime = this.modifiedTime,
            points = this.points ?: 0L
        )
    }

    override fun getCollectionRowsFlow(collectionId: Long?): Flow<List<CollectionRowDTO>> {
        return sqlStorage
            .getCollectionRows(collectionId = collectionId)
            .map {
                it.map { d -> d.toDTO() }
            }
            .flowOn(io)
    }

    override fun getCollectionRowsFlowByFileRelativePath(relativePath: String): Flow<List<CollectionRowDTO>> {
        return sqlStorage
            .getCollectionRowsByFileRelativePath(relativePath = relativePath)
            .map {
                it.map { d -> d.toDTO() }
            }
            .flowOn(io)
    }

    override fun getCollectionRowsCount(collectionId: Long?): Flow<Long> {
        return sqlStorage
            .getCollectionRowsCount(collectionId = collectionId)
            .flowOn(io)
    }

    override fun getAnnotationsFlow(): Flow<List<AnnotationDTO>> {
        return sqlStorage.getAnnotationsFlow().map {
            it.map {
                AnnotationDTO(
                    id = it.id,
                    dataPath = it.dataPath,
                    dataPoint = it.dataPoint,
                    description = it.description,

                    x = it.posX.toFloat(),
                    y = it.posY.toFloat(),
                    width = it.width.toFloat(),
                    height = it.height.toFloat(),
                    modifiedTime = it.modifiedTime,
                    rowId = it.rowId
                )
            }
        }.flowOn(io)
    }

    override fun getTagFilesFlow(): Flow<List<TagLinkDTO>> {
        return sqlStorage
            .getTagFiles()
            .map {
                it.mapNotNull {
                    var data: TagLinkDtoData? = null
                    try {
                        data = TagLinkDtoData.FileNode(it.fileNodeRelativePath)
                    } catch (exc: Exception) {
                    }
                    if (data != null)
                        TagLinkDTO(
                            id = it.id,
                            tagId = it.tagId,
                            data = data
                        )
                    else null
                }
            }
            .flowOn(io)
    }

    override fun getTagAnnotationsFlow(): Flow<List<TagAnnotationDTO>> {
        return sqlStorage
            .getTagAnnotations()
            .map {
                it.map {
                    TagAnnotationDTO(
                        id = it.id,
                        tagId = it.tagId,
                        annotationId = it.annotationId
                    )
                }
            }
            .flowOn(io)
    }

    override fun getCollectionRowsPaginated(
        offset: Long,
        pageSize: Long,
        collectionId: Long
    ): Flow<List<CollectionRowDTO>> {
        return sqlStorage
            .getCollectionRowsPaginated(
                collectionId = collectionId,
                offset = offset,
                pageSize = pageSize
            ).map { d ->
                d.map { it.toDTO() }
            }.flowOn(io)
    }

    override fun getCollectionRowFlow(rowId: Long): Flow<CollectionRowDTO?> {
        return sqlStorage
            .getCollectionRow(rowId = rowId)
            .map { d -> d?.toDTO() }
            .flowOn(io)
    }

    fun IndexFile.dto(): IndexFileDto {
        val syncStatus = when (this.serverSyncStatus) {
            SyncStatus.SYNCED.code ->
                SyncStatus.SYNCED

            SyncStatus.DIRTY.code ->
                SyncStatus.DIRTY

            SyncStatus.NEW.code ->
                SyncStatus.NEW

            SyncStatus.DELETED.code ->
                SyncStatus.DELETED

            else -> null
        }
        return IndexFileDto(
            relativePath = this.relativePath,
            contentHash = this.contentHash,
            modifiedTime = this.modifiedTime,
            size = this.size,
            isDirectory = this.isDirectory == 1L,
            lastSyncedHash = this.lastSyncedHash,
            serverSyncStatus = syncStatus
        )
    }

    override fun getIndexFilesFlow(): Flow<List<IndexFileDto>> {
        return sqlStorage
            .getIndexFilesFlow()
            .map { d -> d.map { it.dto() } }
            .flowOn(io)
    }

    override fun getIndexFiles(): List<IndexFileDto> {
        return sqlStorage
            .getIndexFiles()
            .map { d -> d.dto() }
    }

    override fun getTagsFlow(): Flow<List<TagDTO>> {
        return sqlStorage.getTagsFlow()
            .map {
                it.map {
                    TagDTO(
                        id = it.id,
                        name = it.name,
                        color = it.color.toColor(),
                        createdTime = it.createdTime,
                        modifiedTime = it.modifiedTime
                    )
                }
            }
            .flowOn(io)
    }

    override fun getTagFlow(id: Long): Flow<TagDTO?> {
        return sqlStorage
            .getTagFlow(id = id)
            .map {
                it?.let {
                    TagDTO(
                        id = it.id,
                        name = it.name,
                        color = it.color.toColor(),
                        createdTime = it.createdTime,
                        modifiedTime = it.modifiedTime
                    )
                }
            }
            .flowOn(io)
    }

    override fun getTagToTagsFlow(): Flow<List<TagToTagDTO>> {
        return sqlStorage
            .getTagToTagsFlow()
            .map {
                it.map {
                    TagToTagDTO(
                        id = it.id,
                        firstTagId = it.firstTagId,
                        secondTagId = it.secondTagId
                    )
                }
            }
            .flowOn(io)
    }

    override fun getTagCollectionRowsFlow(): Flow<List<TagCollectionRowDTO>> {
        return sqlStorage
            .getTagCollectionRows()
            .map {
                it.map {
                    TagCollectionRowDTO(
                        id = it.id,
                        tagId = it.tagId,
                        rowId = it.rowId
                    )
                }
            }
            .flowOn(io)
    }

    override fun getWorkspace(): WorkspacePresentation {
        return workspace
    }

    override fun isWorkspaceExists(): Boolean {
        val result =
            filesRepository.isNodeExists(
                FileTreeNode.Directory.create(
                    getWorkspace().absolutePath,
                    getWorkspace().fullpath
                )
            )
        println("workspace env - isWorkspaceExists(): $result")
        return result
    }

    override suspend fun createFileNode(
        parentRelativePath: String,
        fileName: FileName,
        isAbsoluteNew: Boolean,
        byteArray: ByteArray?,
    ): ResultWrapper<FileTreeNode.File, String> {
        val newNode: ResultWrapper<FileTreeNode, String> = if (isAbsoluteNew) {
            var index = 0
            var newNameFileNode: FileTreeNode.File?
            while (true) {
                newNameFileNode = FileTreeNode.File(
                    name = fileName.copy(name = fileName.name + index.toString()),
                    parentRelativePath = parentRelativePath,
                    workspaceFullPath = getWorkspace().absolutePath,
                    fileSize = 0L,
                )
                if (!filesRepository.isNodeExists(newNameFileNode)) {
                    break
                }
                index++
            }
            filesRepository.createNode(
                workspacePath = workspace.absolutePath,
                newNameFileNode,
                byteArray = byteArray
            )
        } else {
            filesRepository.createNode(
                workspacePath = workspace.absolutePath,
                FileTreeNode.File(
                    name = fileName,
                    parentRelativePath = parentRelativePath,
                    workspaceFullPath = getWorkspace().absolutePath,
                    fileSize = 0L,
                ),
                byteArray = byteArray
            )
        }

        return resultBlock {
            val fileNode = bind(newNode)
            ensure(fileNode is FileTreeNode.File) { "Expected file to be created" }
            se(parentRelativePath, fileNode)
            fileNode
        }
    }

    private suspend fun se(parentRelativePath: String, createdNode: FileTreeNode) {
        updateTimes()
//        val filesState = fileNodesState.value
//        if (filesState is UIState.Success) {
//            val files = filesState.data.insertNode(createdNode, parentRelativePath)
//            fileNodesState.emit(UIState.Success(files))
//        }
    }

    override suspend fun createDirectory(
        parentFolder: FileTreeNode.Directory?,
        name: String,
        isAbsoluteNew: Boolean
    ): ResultWrapper<FileTreeNode.Directory, String> {
        val newNode = if (isAbsoluteNew) {
            var index = 0
            var selectedDir: FileTreeNode.Directory?
            while (true) {
                val directory = FileTreeNode.Directory(
                    name = name + index.toString(),
                    parentRelativePath = parentFolder?.getRelativePath() ?: "",
                    children = listOf(),
                    fileSize = 0L,
                    workspaceFullPath = getWorkspace().absolutePath
                )
                if (!filesRepository.isNodeExists(directory)) {
                    selectedDir = directory
                    break
                }
                index++
            }
            filesRepository.createNode(workspacePath = workspace.absolutePath, selectedDir)
        } else {
            filesRepository.createNode(
                workspacePath = workspace.absolutePath,
                FileTreeNode.Directory(
                    name = name,
                    parentRelativePath = parentFolder?.getRelativePath() ?: "",
                    children = listOf(),
                    fileSize = 0L,
                    workspaceFullPath = getWorkspace().absolutePath,
                )
            )
        }
        return resultBlock {
            val fileNode = bind(newNode)
            ensure(fileNode is FileTreeNode.Directory) { "Expected directory to be created" }

            val deletedFiles = getDeletedFilesMetadata().toMutableMap()
            if (deletedFiles.contains(fileNode.getRelativePath())) {
                deletedFiles.remove(fileNode.getRelativePath())
                saveDeletedMetadata(deletedFiles)
            }
            if (parentFolder != null) {
                se(parentFolder.getRelativePath(), fileNode)
            }
            fileNode
        }
    }

    data class RenamedFileSnap(
        val oldNode: FileTreeNode,
        val renamed: FileTreeNode
    )

    private fun renameAllChildNodes(
        newDirectoryRelativePath: String,
        children: List<FileTreeNode>?
    ): List<RenamedFileSnap> {
        val listOfOldChilds = mutableListOf<RenamedFileSnap>()
        if (children == null)
            return listOfOldChilds
        for (child in children) {
            val oldRelativePath = child.getRelativePath()

            val newNode = when (child) {
                is FileTreeNode.Directory -> child.copy(
                    parentRelativePath = newDirectoryRelativePath
                )

                is FileTreeNode.File -> child.copy(
                    parentRelativePath = newDirectoryRelativePath
                )
            }
            listOfOldChilds.add(
                RenamedFileSnap(
                    oldNode = child,
                    renamed = newNode
                )
            )
            sqlStorage.renameFileNode(
                oldRelativePath = oldRelativePath,
                newRelativePath = newNode.getRelativePath()
            )
            if (newNode.isDirectory()) {
                listOfOldChilds.addAll(
                    renameAllChildNodes(
                        newDirectoryRelativePath = newNode.getRelativePath(),
                        newNode.getChildrenOrNull()
                    )
                )
            }
        }
        return listOfOldChilds
    }

    override suspend fun renameNode(
        oldNode: FileTreeNode,
        newNode: FileTreeNode
    ): ResultWrapper<FileTreeNode, String> {
        return resultBlock {
            //todo remove unnecessary
            val oldNode = getNodes(null).getAll(true)
                .firstOrNull { b -> b.getFullPath() == oldNode.getFullPath() }!!

            ensure(newNode.getShortName().isNotEmpty()) { "new name is empty" }
            ensure(filesRepository.isNodeExists(oldNode)) { "file is not exists" }
            ensure(!filesRepository.isNodeExists(newNode)) { "target path is already exists" }

            val oldRelativePath = oldNode.getRelativePath()
            val updatedNode = bind(
                filesRepository.moveNode(
                    workspacePath = workspace.absolutePath,
                    oldNode,
                    newNode
                )
            )
            val deletedFiles = getDeletedFilesMetadata().toMutableMap()

            if (oldNode.isDirectory()) {
                val listOfRenamedSnaps = renameAllChildNodes(
                    newDirectoryRelativePath = updatedNode.getRelativePath(),
                    children = oldNode.getChildrenOrNull()
                )
                for (snap in listOfRenamedSnaps) {
                    val oldNodePath = snap.oldNode.getRelativePath()
                    val newNodePath = snap.renamed.getRelativePath()
                    sqlStorage.renameFileNode(
                        oldRelativePath = oldNodePath,
                        newRelativePath = newNodePath
                    )
                    fileManagerService.movedFile(
                        oldNodePath,
                        newNodePath
                    )

                    val oldDeleteFile = deletedFiles[oldNodePath]
                    deletedFiles[oldNodePath] = oldDeleteFile.createIndexForDeletion(snap.oldNode)
                    if (deletedFiles.contains(newNodePath)) {
                        deletedFiles.remove(newNodePath)
                    }
                }
            }
            val newRelativePath = newNode.getRelativePath()
            sqlStorage.renameFileNode(
                oldRelativePath = oldRelativePath,
                newRelativePath = newRelativePath
            )

            fileManagerService.movedFile(
                oldNode.getRelativePath(),
                newNode.getRelativePath()
            )

            val oldIndex = deletedFiles[oldRelativePath]
            deletedFiles[oldRelativePath] = oldIndex.createIndexForDeletion(oldNode)
            if (deletedFiles.contains(newRelativePath)) {
                deletedFiles.remove(newRelativePath)
            }
            saveDeletedMetadata(deletedFiles)

            updateTimes()

            updatedNode
        }
    }

    private fun IndexFileDto?.createIndexForDeletion(
        node: FileTreeNode,
        modifiedTime: Long = nowInMs()
    ): IndexFileDto {
        return if (this != null) {
            this.copy(
                modifiedTime = modifiedTime,
                serverSyncStatus = SyncStatus.DELETED
            )
        } else {
            IndexFileDto(
                relativePath = node.getRelativePath(),
                contentHash = null,
                modifiedTime = modifiedTime,
                size = null,
                isDirectory = node is FileTreeNode.Directory,
                lastSyncedHash = null,
                serverSyncStatus = SyncStatus.DELETED
            )
        }
    }

    override fun renameCollection(request: RenameDataCollectionRequest) {
        require(request.newName.isNotEmpty()) { "new name is empty" }
        sqlStorage.renameCollection(request = request)
    }

    override fun renameTag(request: RenameTagRequest) {
        require(request.newName.isNotEmpty()) { "new name is empty" }
        sqlStorage.renameTag(request = request)
    }

    override suspend fun copyFile(
        newFile: FileTreeNode.File,
        byteArray: ByteArray?
    ) {
        if (filesRepository.isNodeExists(newFile)) {
            doNothing()
            return
        }
        filesRepository.createNode(
            workspacePath = workspace.absolutePath,
            node = newFile,
            byteArray = byteArray
        )
        updateTimes()
    }

    override fun updateTag(request: UpdateTagRequest): ResultWrapper<Unit, String> {
        return sqlStorage.updateTag(request = request)
    }

    override fun renameCollectionRow(request: RenameDataCollectionRowRequest) {
        require(request.newName.isNotEmpty()) { "new name is empty" }
        sqlStorage.renameCollectionRow(request = request)
    }

    @OptIn(ExperimentalTime::class)
    override fun updateCollection(request: UpdateDataCollectionRequest) {
        sqlStorage.updateCollection(request = request)
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun deleteNode(node: FileTreeNode) {
        deleteNodes(listOf(node))
    }

    private fun internalDeleteNodes(child: FileTreeNode): List<FileTreeNode> {
        val files = mutableListOf<FileTreeNode>()
        files.add(child)
        if (child is FileTreeNode.Directory) {
            for (childNode in child.children) {
                files.addAll(internalDeleteNodes(childNode))
            }
        }
        filesRepository.deleteNode(child)
        return files
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun deleteNodes(nodesToDelete: List<FileTreeNode>) {
        val metadataList = getDeletedFilesMetadata().toMutableMap()
        for (node in nodesToDelete) {
            val files = internalDeleteNodes(node)
            for (node in files) {
                val relativePath = node.getRelativePath()
                val metadataOld = metadataList[relativePath]
                metadataList[relativePath] = metadataOld.createIndexForDeletion(node)
            }
        }
        saveDeletedMetadata(metadataList)
        updateTimes()
    }

    override fun getNodeText(node: FileTreeNode.File): ResultWrapper<String, String> {
        return filesRepository.getNodeText(node)
    }

    override suspend fun setNodeText(
        node: FileTreeNode.File,
        text: String
    ): ResultWrapper<Unit, String> {
        return filesRepository.setNodeText(node, text)
    }

    override fun syncDirtyFiles(list: List<IndexFileDto>): ResultWrapper<Unit, String> {
        return sqlStorage.syncDirtyFiles(list)
    }

    override fun deleteIndexFiles(list: List<String>): ResultWrapper<Unit, String> {
        return sqlStorage.deleteIndexFiles(list)
    }

    override fun updateIndexFiles(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String> {
        return sqlStorage.updateIndexFiles(
            localNodes = localNodes,
            serverNodes = serverNodes
        )
    }

    override fun closeDatabase() {
        sqlStorage.close()
    }

    override fun updateIndexFilesLocal(localNodes: List<FileTreeNode>): ResultWrapper<Unit, String> {
        return sqlStorage.updateIndexFilesLocal(
            localNodes = localNodes
        )
    }

    override fun syncAllIndexes(specificIndexes: List<IndexFileDto>): ResultWrapper<Unit, String> {
        return sqlStorage.syncAllFiles(specificIndexes = specificIndexes)
    }

    override fun setFilesAsSynced(
        paths: List<String>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String> {
        return sqlStorage.setFilesAsSynced(
            paths = paths,
            serverNodes = serverNodes
        )
    }

    override fun createCollection(request: CreateCollectionRequest): ResultWrapper<Long, String> {
        return sqlStorage.createCollection(request = request)
    }

    override fun createCollectionRow(request: CreateCollectionRowRequest): ResultWrapper<Long, String> {
        return sqlStorage.createCollectionRow(request = request)
    }

    override fun updateCollectionRow(request: UpdateDataCollectionRowRequest) {
        sqlStorage.updateCollectionRow(request = request)
    }

    override fun updateRowsForPdf(request: UpdateRowsByPdf) {
        sqlStorage.updateRowsForPdf(request = request)
    }

    override fun deleteCollectionRow(id: Long) {
        sqlStorage.deleteCollectionRow(id = id)
    }

    override fun deleteCollection(id: Long) {
        sqlStorage.deleteCollection(id = id)
    }


    @OptIn(ExperimentalTime::class)
    override suspend fun createAnnotation(data: CreateAnnotationRequest): ResultWrapper<Long, String> {
        return sqlStorage.createAnnotation(
            annotation = Annotation(
                id = 0L,
                dataPath = data.dataPath.normalizeText(),
                dataPoint = data.dataPoint,
                description = data.description,
                createdTime = nowInMs(),
                modifiedTime = nowInMs(),
                posX = data.x.toDouble(),
                posY = data.y.toDouble(),
                width = data.width.toDouble(),
                height = data.height.toDouble(),
                rowId = data.rowId
            )
        )
    }

    override fun createTag(request: CreateTagRequest): ResultWrapper<Long, String> {
        return sqlStorage.createTag(
            Tag(
                id = 0L,
                name = request.name,
                color = request.color.toHexString(),
                createdTime = request.createdTime,
                modifiedTime = request.createdTime,
                description = ""
            )
        )
    }

    override fun createTagToTag(request: CreateTagToTagRequest): ResultWrapper<Long, String> {
        return sqlStorage.createTagToTag(request = request)
    }

    override fun createTagLink(request: CreateTagLinkRequest) {
        sqlStorage.addTagLink(
            relativePath = request.relativePath,
            tagId = request.tagId
        )
    }

    override fun createTagCollectionRow(request: CreateTagCollectionRowRequest) {
        sqlStorage.createTagCollectionRow(request = request)
    }

    override fun deleteTagLink(id: Long) {
        sqlStorage.deleteTagLink(id = id)
    }

    override fun deleteTag(id: Long) {
        sqlStorage.deleteTag(id = id)
    }

    override fun deleteAnnotation(id: Long) {
        sqlStorage.deleteAnnotation(id = id)
    }

    override fun deleteTagToTag(id: Long) {
        sqlStorage.deleteTagToTag(id = id)
    }

    override fun deleteTagCollectionRow(id: Long) {
        sqlStorage.deleteTagCollectionRow(id = id)
    }

    override suspend fun createDatabase() {
        sqlStorage.createDatabase()
    }

    override suspend fun createDatabaseFiles() {
        sqlStorage.createDbFiles()
    }

    override suspend fun createIndexDatabaseFiles() {
        sqlStorage.createIndexDbFiles()
    }

    override suspend fun saveDeletedMetadata(list: Map<String, IndexFileDto>): ResultWrapper<Unit, String> {
        return resultBlock {
            sqlStorage.syncIndexDeletedFiles(list)
        }
    }

    override fun getDeletedFilesMetadata(): Map<String, IndexFileDto> {
        return sqlStorage.getIndexFiles()
            .filter { d -> d.serverSyncStatus == SyncStatus.DELETED.code }
            .associate { d ->
                d.relativePath to d.dto()
            }
    }
}