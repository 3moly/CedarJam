package com.moly3.cedarjam.core.domain.service

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.hideHiddenDirectory
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagToTagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.node.GraphSettingsConfig
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphPresentation
import com.moly3.cedarjam.core.domain.model.node.toPresentation
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceFont
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment.Companion.getHiddenDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceSession(
    private val filesRepository: IFilesRepository,
    private val appEnvironment: IAppEnvironment,
    private val scope: CoroutineScope,
    private val workspace: IWorkspaceEnvironment,
    val fileManagerService: FileManagerService,
) {

    fun getSettingsFlow(): StateFlow<WorkspaceSettings> {
        return workspace.getWorkspaceSettingsFlow()
    }

    suspend fun setSettings(settings: WorkspaceSettings) {
        workspace.setWorkspaceSettings(settings)
    }

    val workspaceEnvStateFlow: StateFlow<IWorkspaceEnvironment> = MutableStateFlow(workspace)

    val workspaceFlow: Flow<WorkspacePresentation> = workspaceEnvStateFlow.map {
        println("val workspaceFlow: Flow<Workspace> ${it.getWorkspace().name}")
        it.getWorkspace()
    }

    val databaseStatusFlow: Flow<UIState<Unit, DatabaseError>> =
        workspaceEnvStateFlow.flatMapLatest { workspaceEnv ->
            workspaceEnv.getDatabaseStatus()
        }.onStart { emit(UIState.Loading) }
    val filesFlow: Flow<UIState<List<FileTreeNode>, String>> =
        workspaceEnvStateFlow.flatMapLatest { workspaceEnv ->
            workspaceEnv.getFileNodesFlow()
        }.onStart { emit(UIState.Loading) }

    fun <T> Flow<T>.shareScope(): Flow<T> {
        return this.shareIn(
            scope = scope,
            started = SharingStarted.Lazily,
            replay = 1
        )
    }

    val collectionsFlow: Flow<List<CollectionDTO>> =
        workspaceEnvStateFlow
            .flatMapLatest {
                it.getCollectionsFlow()
            }.shareScope()

    val collectionRowsFlow: Flow<List<CollectionRowDTO>> =
        workspaceEnvStateFlow
            .flatMapLatest {
                println("flatMapLatest getCollectionRowsFlow")
                it.getCollectionRowsFlow(null)
            }.shareScope()

    val tagsFlow: Flow<List<TagDTO>> =
        workspaceEnvStateFlow
            .flatMapLatest {
                it.getTagsFlow()
            }.shareScope()

    val tagToTagsFlow: Flow<List<TagToTagDTO>> =
        workspaceEnvStateFlow
            .flatMapLatest {
                it.getTagToTagsFlow()
            }.shareScope()

    val tagCollectionRowsFlow: Flow<List<TagCollectionRowDTO>> =
        workspaceEnvStateFlow
            .flatMapLatest {
                it.getTagCollectionRowsFlow()
            }.shareScope()

    val tagLinksFlow: Flow<List<TagLinkDTO>> =
        workspaceEnvStateFlow
            .flatMapLatest {
                it.getTagLinksFlow()
            }.shareScope()

    private val resourcesFlow: Flow<UIState<List<FileTreeNode>, Nothing>> = combine(
        filesFlow,
        workspaceFlow
    ) { files, workspace ->
        if (files !is UIState.Success) {
            Logger.d("resourcesFlow: ${files}")
            UIState.Loading
        } else {
            UIState.Success(
                getHiddenDirectory(
                    workspace = workspace,
                    name = "Resources",
                    directoryName = IWorkspaceEnvironment.hiddenResources,
                    files = files.data
                )
            )
        }
    }.shareScope()

    val getFileNodes: Flow<UIState<List<FileTreeNode>, String>> = combine(
        filesFlow,
        workspaceFlow
    ) { files, workspace ->
        if (files !is UIState.Success) {
            UIState.Error("workspace is not selected")
        } else {
            val directory = files.data.first()
            if (directory is FileTreeNode.Directory) {
                val childrens = directory.children.hideHiddenDirectory()
                UIState.Success(listOf(directory.copy(children = childrens)))
            } else {
                UIState.Success(listOf(directory))
            }
        }
    }

    fun getResources(): Flow<UIState<List<FileTreeNode>, Nothing>> {
        return resourcesFlow
    }

    val graphEco by lazy {
        ObsGraphEco(
            scope = scope,
            appEnvironment = appEnvironment,
            workspaceSession = this,
            startTargetId = null,
            config = GraphSettingsConfig.Default.copy(
                isShowDirectories = false,
                isOrphans = true
            )
        )
    }

    fun getConnectionPresentations(graphNodeTagId: String): Flow<List<ObsidianGraphPresentation>> {
        return com.moly3.cedarjam.core.domain.func.combine(
            graphEco.nodes,
            graphEco.connectionsFlow,
            tagsFlow,
            collectionsFlow,
            collectionRowsFlow,
            filesFlow
        ) { nodes, connections, tags, collections, rows, filesState ->
            val found = connections[graphNodeTagId]
            val list = if (found != null) {
                nodes.filter { d -> found.contains(d.id) }.map { x -> x.data }
            } else null
            val files = mutableListOf<FileTreeNode>()
            filesState.map {
                files.addAll(files)
            }
            list?.toPresentation(
                tags = tags,
                collections = collections,
                rows = rows,
                files = files
            ) ?: listOf()
        }
    }

    private val _workspaceFont = MutableStateFlow<WorkspaceFont?>(null)
    val workspaceFont = _workspaceFont.asStateFlow()

    suspend fun initConfigAndFiles() {
        workspace.initConfigAndFiles()
    }

    suspend fun loadLocalFont(newFile: FileTreeNode.File? = null) {
        val node = FileTreeNode.File(
            name = FileName(name = "default", extension = "otf"),
            pathWrapper(workspace.getWorkspace().absolutePath, hiddenDirectory).pathString
        )
        val newNode = newFile ?: node
        val font = try {
            if (filesRepository.isNodeExists(newNode)) {
                WorkspaceFont(newNode, timestamp = nowInMs())
            } else
                null
        } catch (exc: Exception) {
            null
        }
        _workspaceFont.emit(font)
    }
}