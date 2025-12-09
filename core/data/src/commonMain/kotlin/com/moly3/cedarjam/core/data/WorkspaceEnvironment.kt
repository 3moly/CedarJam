package com.moly3.cedarjam.core.data

import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.data.DataCollectionRow
import com.moly3.data.Tag
import com.moly3.cedarjam.core.domain.DefaultJson
import com.moly3.cedarjam.core.domain.func.doNothing
import com.moly3.cedarjam.core.domain.func.getRelativePath
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.func.toColor
import com.moly3.cedarjam.core.domain.func.toHexString
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.AnnotationDTO
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.DeletedFileMetadata
import com.moly3.cedarjam.core.domain.model.FileMetadata
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileStructure
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.ResultWrapper
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
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
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
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.storage.ISqlStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.collections.map
import kotlin.time.Clock
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

    private val deletedFilesMeta = FileTreeNode.File(
        name = FileName(name = "deleted_files", extension = null),
        parentPath = pathWrapper(
            workspace.absolutePath,
            hiddenDirectory
        ).pathString
    )

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

    override fun getNodes(parentFolder: FileTreeNode.Directory?): List<FileTreeNode> {
        val directoryNode = parentFolder ?: FileTreeNode.Directory.create(workspace.absolutePath)
        return filesRepository.getNodes(directoryNode)
    }

    private fun tryToGet(): UIState<List<FileTreeNode>, String> {
        return try {
            UIState.Success(getNodes(null))
        } catch (exc: Exception) {
            UIState.Error(exc.message ?: "")
        }
    }

    override suspend fun uploadSync(
        archiveFullPath: String,
        metadata: List<FileMetadata>,
        filesToDownload: List<String>,
    ): ResultWrapper<ByteArray, String> {
        return resultBlock {
            try {
                val fileNode = filesRepository.getFileNodeFromFullPath(
                    archiveFullPath,
                    isDirectory = false
                ) as FileTreeNode.File
                val byteArray = filesRepository.getNodeBytes(fileNode)
                val uploadResult = syncNetRepository.upload(
                    userName = "bulat",
                    workspaceName = workspace.name,
                    metadata = metadata,
                    filesToDownload = filesToDownload,
                    archiveByteArray = byteArray
                )
                uploadResult.shouldBeSuccess()
                uploadResult.value
            } catch (aa: ArithmeticException) {
                raise(aa.message ?: "")
            } catch (exc: Exception) {
                raise("uploadSync: ${exc.message}")
            }
        }
    }

    override suspend fun updateTimes() {
        val state = tryToGet()
        fileNodesState.emit(state)
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
            workspaceName = workspace.name
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
            modifiedTime = this.modifiedTime
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun getCollectionRowsFlow(collectionId: Long?): Flow<List<CollectionRowDTO>> {
        return sqlStorage
            .getCollectionRows(collectionId = collectionId)
            .map {
                println(
                    "${collectionId} rows: mapping to dto ${
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    }"
                )
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
        return flowOf(listOf())
//        return sqlStorage.getAnnotationsFlow().map {
//            it.map {
//                AnnotationDTO(
//                    id = it.id,
//                    dataPath = it.dataPath,
//                    dataPoint = it.dataPoint,
//                    description = it.description
//                )
//            }
//        }.flowOn(core.domain.io)
    }

    override fun getTagLinksFlow(): Flow<List<TagLinkDTO>> {
        return sqlStorage
            .getTagLinks()
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
            filesRepository.isNodeExists(FileTreeNode.Directory.create(getWorkspace().fullpath))
        println("workspace env - isWorkspaceExists(): $result")
        return result
    }


    override suspend fun createFileNode(
        parentFolder: FileTreeNode.Directory?,
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
                    parentPath = parentFolder?.getFullPath() ?: workspace.fullpath,
                    fileSize = 0L
                )
                if (!filesRepository.isNodeExists(newNameFileNode)) {
                    break
                }
                index++
            }
            filesRepository.createNode(
                newNameFileNode,
                byteArray = byteArray
            )
        } else {
            filesRepository.createNode(
                FileTreeNode.File(
                    name = fileName,
                    parentPath = parentFolder?.getFullPath() ?: workspace.fullpath,
                    fileSize = 0L
                ),
                byteArray = byteArray
            )
        }

        return resultBlock<FileTreeNode.File, String> {
            val fileNode = bind(newNode)
            ensure(fileNode is FileTreeNode.File) { "Expected file to be created" }
            updateTimes()
            fileNode as FileTreeNode.File
        }
    }

    override suspend fun createDirectory(
        parentFolder: FileTreeNode.Directory?,
        name: String,
        isAbsoluteNew: Boolean
    ): ResultWrapper<FileTreeNode.Directory, String> {
        val newNode = if (isAbsoluteNew) {
            var index = 0
            var ff: FileTreeNode.Directory?
            while (true) {
                val ss = FileTreeNode.Directory(
                    name = name + index.toString(),
                    parentPath = parentFolder?.getFullPath() ?: workspace.fullpath,
                    children = listOf(),
                    fileSize = 0L
                )
                if (!filesRepository.isNodeExists(ss)) {
                    ff = ss
                    break
                }
                index++
            }
            filesRepository.createNode(ff, byteArray = null)
        } else {
            filesRepository.createNode(
                FileTreeNode.Directory(
                    name = name,
                    parentPath = parentFolder?.getFullPath() ?: workspace.fullpath,
                    children = listOf(),
                    fileSize = 0L
                ),
                byteArray = null
            )
        }
        return resultBlock {

            val fileNode = bind(newNode)
            ensure(fileNode is FileTreeNode.Directory) { "Expected directory to be created" }
            updateTimes()
            fileNode as FileTreeNode.Directory
        }
    }

    private fun renameDD(
        oldDirectoryRelativePath: String,
        newDirectoryRelativePath: String,
        children: List<FileTreeNode>?
    ) {
        if (children == null)
            return
        for (child in children) {
            val oldRelativePath = child.getRelativePath(workspacePath = workspace.fullpath)

            val newNode = when (child) {
                is FileTreeNode.Directory -> child.copy(
                    parentPath = child.parentPath.replaceFirst(
                        oldDirectoryRelativePath,
                        newDirectoryRelativePath
                    )
                )

                is FileTreeNode.File -> child.copy(
                    parentPath = child.parentPath.replaceFirst(
                        oldDirectoryRelativePath,
                        newDirectoryRelativePath
                    )
                )
            }
            sqlStorage.renameFileNode(
                oldRelativePath = oldRelativePath,
                newRelativePath = newNode.getRelativePath(workspacePath = workspace.fullpath)
            )
            if (newNode.isDirectory()) {
                renameDD(
                    oldDirectoryRelativePath = oldRelativePath,
                    newDirectoryRelativePath = newNode.getRelativePath(workspacePath = workspace.fullpath),
                    newNode.getChildrenOrNull()
                )
            }
        }
    }

    override suspend fun renameNode(
        oldNode: FileTreeNode,
        newNode: FileTreeNode
    ): ResultWrapper<FileTreeNode, String> {
        return resultBlock {

            ensure(newNode.getShortName().isNotEmpty()) { "new name is empty" }
            ensure(filesRepository.isNodeExists(oldNode)) { "file is not exists" }
            ensure(!filesRepository.isNodeExists(newNode)) { "target path is already exists" }

            val oldRelativePath = oldNode.getRelativePath(workspacePath = workspace.fullpath)
            val updatedNode = bind(
                filesRepository.moveNode(
                    oldNode,
                    newNode
                )
            )
            if (oldNode.isDirectory()) {
                renameDD(
                    oldDirectoryRelativePath = oldRelativePath,
                    newDirectoryRelativePath = updatedNode.getRelativePath(workspacePath = workspace.fullpath),
                    oldNode.getChildrenOrNull()
                )
            }
            val newRelativePath = newNode.getRelativePath(workspacePath = workspace.fullpath)
            sqlStorage.renameFileNode(
                oldRelativePath = oldRelativePath,
                newRelativePath = newRelativePath
            )
            updateTimes()
            fileManagerService.movedFile(
                oldNode,
                newNode
            )
            val deletedFiles = getDeletedFilesMetadata().toMutableMap()
            deletedFiles[oldRelativePath] = nowInMs()
            deletedFiles.remove(newRelativePath)
            saveDeletedMetadata(deletedFiles)

            updatedNode
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
        originalFullPath: String,
        newFile: FileTreeNode.File,
        byteArray: ByteArray?
    ) {
        if (filesRepository.isNodeExists(newFile)) {
            doNothing()
            return
        }
        filesRepository.createNode(
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

    @OptIn(ExperimentalTime::class)
    override suspend fun deleteNodes(nodes: List<FileTreeNode>) {
        val metadataList = getDeletedFilesMetadata().toMutableMap()

        for (node in nodes) {
            val relativePath = node.getRelativePath(workspacePath = workspace.absolutePath)
            val addedTime = Clock.System.now().toEpochMilliseconds()
            metadataList[relativePath] = addedTime
            if (node is FileTreeNode.Directory) {
                for (item in node.children) {
                    deleteNode(item)
                }
            }
            filesRepository.deleteNode(node)
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

    override fun createCollection(request: CreateCollectionRequest): ResultWrapper<Long, String> {
        return sqlStorage.createCollection(request = request)
    }

    override fun createCollectionRow(request: CreateCollectionRowRequest): ResultWrapper<Long, String> {
        return sqlStorage.createCollectionRow(request = request)
    }

    override fun updateCollectionRow(request: UpdateDataCollectionRowRequest) {
        sqlStorage.updateCollectionRow(request = request)
    }

    override fun deleteCollectionRow(id: Long) {
        sqlStorage.deleteCollectionRow(id = id)
    }

    override fun deleteCollection(id: Long) {
        sqlStorage.deleteCollection(id = id)
    }


    @OptIn(ExperimentalTime::class)
    override fun createAnnotation(data: AnnotationDTO) {
//      todo  val createTime = Clock.System.now().toEpochMilliseconds()
//
//        sqlStorage.createAnnotation(
//            data = Annotation(
//                id = data.id,
//                dataPath = data.dataPath,
//                dataPoint = data.dataPoint,
//                description = data.description,
//                createdTime = createTime,
//                modifiedTime = createTime
//            )
//        )
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
            relativePath = getRelativePath(
                fullPath = request.fullPath,
                workspacePath = workspace.fullpath
            ),
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

    override suspend fun saveDeletedMetadata(list: Map<String, Long>): ResultWrapper<Unit, String> {
        val json = DefaultJson.encodeToString(list.map { d ->
            DeletedFileMetadata(
                relativePath = d.key,
                deletedTime = d.value
            )
        })
        return setNodeText(deletedFilesMeta, json)
    }

    override fun getDeletedFilesMetadata(): Map<String, Long> {
        var deletedMetadata = mutableListOf<DeletedFileMetadata>()
        val deletedNode = getNodeText(node = deletedFilesMeta)
        when (deletedNode) {
            is ResultWrapper.Error -> {}

            is ResultWrapper.Success -> {
                try {
                    deletedMetadata = DefaultJson.decodeFromString(deletedNode.value)
                } catch (exc: Exception) {
                }
            }
        }
        return deletedMetadata.associate { Pair(it.relativePath, it.deletedTime) }
    }
}