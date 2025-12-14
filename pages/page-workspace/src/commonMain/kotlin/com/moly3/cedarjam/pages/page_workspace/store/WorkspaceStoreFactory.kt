package com.moly3.cedarjam.pages.page_workspace.store

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.navigation.Route.Collection
import com.moly3.cedarjam.navigation.Route.MainGraph
import com.moly3.cedarjam.navigation.Route.MainHome
import com.moly3.cedarjam.navigation.Route.Tag
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.Label
import com.moly3.cedarjam.pages.page_workspace.State
import com.moly3.cedarjam.pages.page_workspace.State.Companion.fromSaveable
import com.moly3.cedarjam.pages.page_workspace.State.Companion.toSaveable
import com.moly3.cedarjam.pages.page_workspace.model.ContextMenuButton
import com.moly3.cedarjam.pages.page_workspace.model.ContextMenuData
import com.moly3.cedarjam.pages.page_workspace.model.RenameFileNodeData
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.func.combine
import com.moly3.cedarjam.core.domain.func.doNothing
import com.moly3.cedarjam.core.domain.func.findNewNameOrDefault
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.openFileInExplorer
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.func.relativeTo
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.fold
import com.moly3.cedarjam.core.domain.model.mapToUIState
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.INavigateToFileUseCase
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.ui.func.recalculateTabWeights
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.pages.page_workspace.Label.*
import com.moly3.cedarjam.pages.page_workspace.store.WorkspaceStore.Msg.*
import com.moly3.cedarjam.pages.page_workspace.ui.internal.MenuCoveredId
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.collections
import com.moly3.cedarjam.ui.files
import com.moly3.cedarjam.ui.graph
import com.moly3.cedarjam.ui.home
import com.moly3.cedarjam.ui.resources
import com.moly3.cedarjam.ui.tags
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

internal class WorkspaceStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val workspaceSession: WorkspaceSession,
    private val onSettingsOpen: () -> Unit
) : KoinComponent {

    private val syncUseCase: ISyncUseCase by inject()
    private val systemFilesManager: IFilesRepository by inject()
    private val messagerService: IMessageService by inject()
    private val dialogColorPickerService: DialogColorPickerService by inject()
    private val dialogDeleteService: DialogDeleteService by inject()
    private val fileManagerService: FileManagerService by lazy {
        workspaceSession.fileManagerService
    }
    private val navigator: Navigator by inject()
    private val navigateToFileUseCase: INavigateToFileUseCase by inject {
        parametersOf(fileManagerService)
    }

    private var _cursorPosition: Offset? = null

    fun create(stateKeeper: StateKeeper): WorkspaceStore = object : WorkspaceStore,
        Store<Intent, State, Label> by storeFactory.create(
            name = WorkspaceStore::class.simpleName,
            initialState = stateKeeper.consumeOrDefault(
                "WorkspaceStore",
                State.SaveableState.serializer(),
                default = State().toSaveable()
            ).fromSaveable(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {
    }.also {
        stateKeeper.register(
            key = "WorkspaceStore",
            strategy = State.SaveableState.serializer()
        ) {
            it.state.toSaveable()
        }
    }

    private fun parseFiles(
        fileNodes: List<FileTreeNode>,
        indexFiles: List<IndexFileDto>
    ): List<FileTreeItemPresentation> {
        val resourceItems = mutableListOf<FileTreeItemPresentation>()


        for (fileNode in fileNodes.sortedBy { b ->
            when (b) {
                is FileTreeNode.Directory -> 0
                is FileTreeNode.File -> 1
            }
        }) {
            val syncStatus =
                indexFiles.firstOrNull { d -> d.relativePath == fileNode.getRelativePath() }?.serverSyncStatus
            val parse = if (fileNode.isDirectory())
                parseFiles(
                    fileNode.getChildrenOrNull() ?: listOf(),
                    indexFiles
                ) else null

            resourceItems.add(
                FileTreeItemPresentation(
                    key = "file: ${fileNode.getFullPath()}",
                    name = CJText.Raw(fileNode.getShortName()),
                    data = when (fileNode) {
                        is FileTreeNode.Directory -> FileTreeItemPresentation.FileTreeItemPresentationData.Directory(
                            fileNode,
                            isDragEnabled = true,
                            syncStatus = syncStatus
                        )

                        is FileTreeNode.File -> FileTreeItemPresentation.FileTreeItemPresentationData.File(
                            fileNode,
                            syncStatus = syncStatus
                        )
                    },
                    fileExtension = when (fileNode) {
                        is FileTreeNode.Directory -> null
                        is FileTreeNode.File -> fileNode.name.extension
                    },
                    children = parse?.toPersistentList()
                )
            )
        }

        return resourceItems
    }

    fun findAndRevealFile(
        targetPath: String,
        files: ImmutableList<FileTreeItemPresentation>,
        openedDirectories: MutableSet<String>
    ): String? {

        fun searchInTree(
            nodes: ImmutableList<FileTreeItemPresentation>,
            targetPath: String
        ): List<String>? {
            for (node in nodes) {
                if (node.key == targetPath) {
                    return listOf(node.key)
                }
                node.children?.let { children ->
                    val pathToTarget = searchInTree(children, targetPath)
                    if (pathToTarget != null) {
                        // prepend current node to path
                        return listOf(node.key) + pathToTarget
                    }
                }
            }
            return null
        }

        val pathInFiles = searchInTree(files, targetPath) ?: return null

        // open all parents, but not the file itself (last element is the target)
        val parentDirectories = pathInFiles.dropLast(1)
        parentDirectories.forEach { dirPath ->
            if (dirPath !in openedDirectories) {
                openedDirectories.add(dirPath)
            }
        }

        return targetPath
    }

    fun findIndexInVisibleList(
        key: String,
        files: ImmutableList<FileTreeItemPresentation>,
        openedDirectories: ImmutableList<String>
    ): Int? {
        var index = 0

        fun traverse(nodes: ImmutableList<FileTreeItemPresentation>): Int? {
            for (node in nodes) {
                if (node.key == key) {
                    return index
                }
                index++
                val nodeChildren = node.children
                if (nodeChildren != null &&
                    node.key in openedDirectories
                ) {
                    val found = traverse(nodeChildren)
                    if (found != null) return found
                }
            }
            return null
        }

        return traverse(files)
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, WorkspaceStore.Msg, Label>(lifecycle) {

        private suspend fun revealFile(targetPath: String): Pair<Set<String>, Int>? {
            val openedDirectories = state().openedDirectories.toMutableSet()
            val revealedPath = findAndRevealFile(
                targetPath = targetPath,
                files = state().files,
                openedDirectories = openedDirectories
            )

            //onIntent(Intent.SetOpenedDirectories(openedDirectories.toPersistentSet()))

            return if (revealedPath != null) {
                // Wait for recomposition after opening directories
                delay(50) // Small delay to allow recomposition

                val itemIndex = findIndexInVisibleList(
                    key = revealedPath,
                    files = state().files,
                    openedDirectories = openedDirectories.toPersistentList()
                )
                Logger.i("index found scroll: ${itemIndex}")
                // Calculate the index and scroll
//                        val itemIndex = calculateItemIndex(targetPath, state, openedDirectories)
                if (itemIndex != null) {
                    // listState.animateScrollToItem(itemIndex)
                    openedDirectories to itemIndex
                } else null
            } else null
        }

        private suspend fun send(targetPath: String) {
            val pair = revealFile(targetPath)
            if (pair != null) {
                dispatch(WorkspaceStore.Msg.SetOpenedDirectories(pair.first.toImmutableSet()))
                publish(Label.ScrollToIndex(pair.second))
            }
        }

        private fun updateSyncStatus() {
            scope.launch {
                syncUseCase.getStatus(workspace = workspaceSession.workspaceEnvStateFlow.value)
            }
        }

        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            lifecycle.doOnResume {
                scope.launch {
                    workspaceSession.workspaceEnvStateFlow.value.updateTimes()
                }

                updateSyncStatus()
            }
            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow.value.getIndexFilesFlow().collectLatest {
                    dispatch(WorkspaceStore.Msg.SetIndexFiles(it.toPersistentList()))
                }
            }
            scopeFromStartToStop.launch {
                updateSyncStatus()
            }
            scopeFromStartToStop.launch {
                workspaceSession.getSettingsFlow().collectLatest {
                    dispatch(WorkspaceStore.Msg.SetWorkspaceSettings(it))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.initConfigAndFiles()
                workspaceSession.loadLocalFont()

                workspaceSession.workspaceFont.collectLatest {
                    dispatch(WorkspaceStore.Msg.SetWorkspaceFont(it))
                }
            }
            scopeFromStartToStop.launch {
                combine(
                    workspaceSession.getFileNodes,
                    workspaceSession.tagsFlow,
                    workspaceSession.collectionsFlow,
                    workspaceSession.getResources(),
                    workspaceSession.workspaceFlow,
                    workspaceSession.indexFilesFlow
                ) { files, tags, collections, resourcesState, workspace, indexFiles ->
                    val presentations = mutableListOf<FileTreeItemPresentation>()

                    presentations.add(
                        FileTreeItemPresentation(
                            key = "home_tab",
                            name = CJText.Res(Res.string.home),
                            data = FileTreeItemPresentation.FileTreeItemPresentationData.Home,
                        )
                    )
                    presentations.add(
                        FileTreeItemPresentation(
                            key = "graph_tab",
                            name = CJText.Res(Res.string.graph),
                            data = FileTreeItemPresentation.FileTreeItemPresentationData.Graph,
                        )
                    )
                    val tagItems = mutableListOf<FileTreeItemPresentation>()
                    for (tag in tags) {
                        tagItems.add(
                            FileTreeItemPresentation(
                                key = "tag: ${tag.id}",
                                name = CJText.Raw(tag.name),
                                data = FileTreeItemPresentation.FileTreeItemPresentationData.Tag(
                                    tag = tag
                                ),
                                backColor = tag.color
                            )
                        )
                    }
                    presentations.add(
                        FileTreeItemPresentation(
                            key = "tags_tab",
                            name = CJText.Res(Res.string.tags),
                            data = FileTreeItemPresentation.FileTreeItemPresentationData.Tags,
                            children = tagItems.toPersistentList()
                        )
                    )


                    val collectionsItems = mutableListOf<FileTreeItemPresentation>()
                    for (collection in collections) {
                        collectionsItems.add(
                            FileTreeItemPresentation(
                                key = "collection: ${collection.id}",
                                name = CJText.Raw(collection.name),
                                data = FileTreeItemPresentation.FileTreeItemPresentationData.Collection(
                                    collection.id
                                ),
                            )
                        )
                    }
                    presentations.add(
                        FileTreeItemPresentation(
                            key = "collections_tab",
                            name = CJText.Res(Res.string.collections),
                            data = FileTreeItemPresentation.FileTreeItemPresentationData.Collections,
                            children = collectionsItems.toPersistentList()
                        )
                    )

                    try {
                        val resourcesDirectory: FileTreeNode.Directory? = when (resourcesState) {
                            is UIState.Error,
                            UIState.Loading -> null

                            is UIState.Success -> {
                                resourcesState.data.firstOrNull() as? FileTreeNode.Directory
                            }
                        }
                        presentations.add(
                            FileTreeItemPresentation(
                                key = "resources_tab",
                                name = CJText.Res(Res.string.resources),
                                data = FileTreeItemPresentation.FileTreeItemPresentationData.Directory(
                                    resourcesDirectory!!,
                                    isDragEnabled = false,
                                    syncStatus = null
                                ),
                                children = parseFiles(
                                    resourcesDirectory.getChildrenOrNull() ?: listOf(),
                                    indexFiles
                                ).toPersistentList()
                            )
                        )
                        val filesDirectory: FileTreeNode.Directory? = when (files) {
                            is UIState.Error,
                            UIState.Loading -> null

                            is UIState.Success -> {
                                files.data.firstOrNull() as? FileTreeNode.Directory
                            }
                        }
                        if (filesDirectory != null) {
                            presentations.add(
                                FileTreeItemPresentation(
                                    key = "files_tab",
                                    name = CJText.Res(Res.string.files),
                                    data = FileTreeItemPresentation.FileTreeItemPresentationData.Directory(
                                        filesDirectory,
                                        isDragEnabled = false,
                                        syncStatus = null
                                    ),
                                    children = parseFiles(
                                        filesDirectory.getChildrenOrNull() ?: listOf(),
                                        indexFiles
                                    ).toPersistentList()
                                )
                            )
                        }


                    } catch (exc: Exception) {
                        val msg = "" + exc.message
                    }


                    presentations
                }.flowOn(io)
                    .collectLatest {
                        dispatch(WorkspaceStore.Msg.SetFileNodes(it.toPersistentList()))
                    }
            }
            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow.collectLatest {
                    dispatch(WorkspaceStore.Msg.SetActiveWorkspace(it.getWorkspace()))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.databaseStatusFlow.collectLatest {
                    Logger.d { "databaseStatusFlow.collectLatest: ${it}" }
                    dispatch(WorkspaceStore.Msg.SetDatabaseStatus(it))
                }
            }

        }

        private fun hideContextMenu() {
            dispatch(WorkspaceStore.Msg.SetContextMenu(null))
        }

        private fun createFile(parentFullPath: String, newFileName: FileName) {
            val parentFolder = systemFilesManager.getFileNodeFromFullPath(
                fullPath = parentFullPath,
                isDirectory = true
            )
            if (parentFolder is FileTreeNode.Directory) {
                scope.launch {
                    workspaceSession.workspaceEnvStateFlow.value.createFileNode(
                        parentFolder = parentFolder,
                        fileName = newFileName,
                        isAbsoluteNew = true,
                        byteArray = null
                    )
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SelectWorkspace -> navigator.navigate(Route.Empty)
                is Intent.OpenContextMenu -> {
                    val buttons = mutableListOf<ContextMenuButton>()
                    when (val data = intent.target.data) {
                        is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> {
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Create markdown file"),
                                    onClick = {
                                        createFile(
                                            parentFullPath = data.fileNode.getFullPath(),
                                            newFileName = FileName("Untitled", "md")
                                        )
                                        updateSyncStatus()
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Upload resource"),
                                    onClick = {
                                        executeIntent(Intent.UploadResource)
//                                        createFile(
//                                            parentFullPath = data.fileNode.getFullPath(),
//                                            newFileName = FileName("Untitled", "md")
//                                        )
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Create canvas file"),
                                    onClick = {
                                        createFile(
                                            parentFullPath = data.fileNode.getFullPath(),
                                            newFileName = FileName("Untitled", "canvas")
                                        )
                                        updateSyncStatus()
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Show in finder"),
                                    onClick = {
//                                        val fileNode = systemFilesManager.getFileNodeFromFullPath(
//                                            fullPath = data.fileNode.getFullPath(),
//                                            is
//                                        )
                                        openFileInExplorer(data.fileNode)
                                    }
                                ))
                            if (data.isDragEnabled) {
                                buttons.add(
                                    ContextMenuButton(
                                        title = CJText.Raw("Rename"),
                                        onClick = {
                                            dispatch(
                                                SetRenameFileNodeData(
                                                    RenameFileNodeData(intent.target)
                                                )
                                            )
                                        }
                                    ))
                                buttons.add(
                                    ContextMenuButton(
                                        title = CJText.Raw("Delete"),
                                        onClick = {
                                            val workspaceEnv =
                                                workspaceSession.workspaceEnvStateFlow.value
                                            val fileNode = data.fileNode
//                                                systemFilesManager.getFileNodeFromFullPath(
//                                                    fullPath = data.fileNode.getFullPath(),
//                                                    isDirectory = true
//                                                )
                                            if (fileNode is FileTreeNode.Directory) {
                                                scope.launch {
                                                    val nodes = workspaceEnv.getNodes(fileNode)
                                                    val result = dialogDeleteService.open(Unit)
                                                    if (result) {
                                                        try {
                                                            for (item in nodes) {
                                                                workspaceEnv.deleteNode(item)
                                                            }
                                                        } catch (exc: Exception) {
                                                            println("exc: ${exc.message}")
                                                        }
                                                        updateSyncStatus()
                                                    }
                                                }
                                            }
                                        }
                                    ))
                            } else {

                            }
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.File -> {
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Show in finder"),
                                    onClick = {
//                                        val fileNode = systemFilesManager.getFileNodeFromFullPath(
//                                            fullPath = data.fileNode.getFullPath()
//                                        )
                                        openFileInExplorer(data.fileNode)
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Rename"),
                                    onClick = {
                                        dispatch(
                                            SetRenameFileNodeData(
                                                RenameFileNodeData(intent.target)
                                            )
                                        )
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Delete"),
                                    onClick = {
                                        val workspaceEnv =
                                            workspaceSession.workspaceEnvStateFlow.value
                                        val fileNode = data.fileNode
//                                            systemFilesManager.getFileNodeFromFullPath(
//                                            fullPath = data.fileNode.getFullPath()
//                                        )
                                        scope.launch {
                                            val result = dialogDeleteService.open(Unit)
                                            if (result) {
                                                try {
                                                    workspaceEnv.deleteNode(fileNode)
                                                    val timestamp =
                                                        fileManagerService.getTimestampByFileNode(
                                                            fileNode = fileNode
                                                        )
                                                    if (timestamp != null) {
                                                        fileManagerService.deleteFile(timestamp)
                                                    }
                                                    updateSyncStatus()
                                                } catch (exc: Exception) {
                                                    println("exc: ${exc.message}")
                                                }
                                            }
                                        }
                                    }
                                ))
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.Collection -> {
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Rename"),
                                    onClick = {
                                        dispatch(
                                            SetRenameFileNodeData(
                                                RenameFileNodeData(intent.target)
                                            )
                                        )
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Delete"),
                                    onClick = {
                                        scope.launch {
                                            val result = dialogDeleteService.open(Unit)
                                            val workspaceEnv =
                                                workspaceSession.workspaceEnvStateFlow.value

                                            if (result) {
                                                workspaceEnv.deleteCollection(data.id)
                                                updateSyncStatus()
                                            }
                                        }
                                    }
                                ))
                        }


                        is FileTreeItemPresentation.FileTreeItemPresentationData.Tag -> {
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Change color"),
                                    onClick = {
                                        scope.launch {
                                            val selectedColor =
                                                dialogColorPickerService.open(data.tag.color)
                                            if (selectedColor != null) {
                                                workspaceSession.workspaceEnvStateFlow.value.updateTag(
                                                    UpdateTagRequest(
                                                        color = selectedColor,
                                                        modifiedTime = nowInMs(),
                                                        id = data.tag.id
                                                    )
                                                )
                                                updateSyncStatus()
                                            }
                                        }
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Rename"),
                                    onClick = {
                                        dispatch(
                                            SetRenameFileNodeData(
                                                RenameFileNodeData(intent.target)
                                            )
                                        )
                                    }
                                ))
                            buttons.add(
                                ContextMenuButton(
                                    title = CJText.Raw("Delete"),
                                    onClick = {
                                        scope.launch {
                                            val result = dialogDeleteService.open(Unit)
                                            val workspaceEnv =
                                                workspaceSession.workspaceEnvStateFlow.value

                                            if (result) {
                                                workspaceEnv.deleteTag(data.tag.id)
                                                updateSyncStatus()
                                            }
                                        }
                                    }
                                ))
                        }

                        FileTreeItemPresentation.FileTreeItemPresentationData.Collections -> {}
                        FileTreeItemPresentation.FileTreeItemPresentationData.Graph -> {}
                        FileTreeItemPresentation.FileTreeItemPresentationData.Home -> {}
                        FileTreeItemPresentation.FileTreeItemPresentationData.Tags -> {}
                    }
                    if (buttons.count() > 0) {
                        val context = ContextMenuData(
                            targetKey = intent.target.key,
                            cursorPosition = _cursorPosition ?: intent.cursorPosition,
                            menuButtons = buttons.toPersistentList()
                        )
                        dispatch(SetContextMenu(context))
                    }
                }

                is Intent.SetLockedMenuUnder -> {
                    dispatch(WorkspaceStore.Msg.SetLockedMenuCovered(intent.value))
                }

                is Intent.SetMenuUnder -> {
                    dispatch(WorkspaceStore.Msg.SetMenuCovered(intent.tab))
                }

                is Intent.SetCursorPosition -> {
                    _cursorPosition = intent.offset
                    dispatch(WorkspaceStore.Msg.SetPosition(_cursorPosition))
                }

                Intent.HideContextMenu -> {
                    scope.launch(Dispatchers.Main.immediate) {
                        hideContextMenu()
                    }
                }

                is Intent.UploadResource -> {
                    scope.launch {
                        val file = FileKit.openFilePicker(type = FileKitType.File())
                        if (file != null) {
                            val bytes: ByteArray = file.readBytes()

                            val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

                            val resourcesDirectory = pathWrapper(
                                workspaceEnv.getWorkspace().fullpath,
                                hiddenDirectory,
                                "resources",
                                file.extension
                            )
                            workspaceEnv.createFileNode(
                                parentFolder = FileTreeNode.Directory.create(
                                    resourcesDirectory.pathString,
                                    listOf()
                                ),
                                fileName = FileName(
                                    name = file.nameWithoutExtension,
                                    extension = file.extension
                                ),
                                isAbsoluteNew = false,
                                byteArray = bytes
                            )
                            updateSyncStatus()
                        }
                    }
                }

                is Intent.SetIsFullMenu -> {
                    dispatch(SetIsFullMenu(intent.value))
                }

                Intent.OpenSettings -> {
                    onSettingsOpen()
                }

                is Intent.SelectActiveTabs -> dispatch(SetActiveTab(intent.index))
                Intent.CreateWorkspace -> {
                    scope.launch {
                        workspaceSession.workspaceEnvStateFlow.value.createDatabase()
                    }
                }

                is Intent.SetPageName -> dispatch(SetCurrentTabData(intent.value))
                is Intent.MoveFile -> {
                    scope.launch {
                        val directory = when (val data = intent.directory.data) {
                            is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> data.fileNode
                            else -> null
                        }
                        Logger.e { "moveFile to directory: ${directory}" }
                        val draggingItemPath = when (val data = intent.file.data) {
                            is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> data.fileNode
                            is FileTreeItemPresentation.FileTreeItemPresentationData.File -> data.fileNode
                            else -> null
                        }
                        val isDirectory = when (val data = intent.file.data) {
                            is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> true
                            is FileTreeItemPresentation.FileTreeItemPresentationData.File -> false
                            else -> null
                        }
                        if (draggingItemPath != null && directory != null && isDirectory != null) {
//                            val oldNode = if (isDirectory) {
//                                systemFilesManager.getFileNodeFromFullPath(
//                                    fullPath = draggingItemPath.getFullPath(),
//                                    isDirectory = isDirectory
//                                )
//                            } else {
//                                systemFilesManager.getFileNodeFromFullPath(
//                                    fullPath = draggingItemPath.getFullPath(),
//                                    isDirectory
//                                )
//                            }
                            val workspaceAbsolute =
                                workspaceSession.workspaceEnvStateFlow.value.getWorkspace().absolutePath
                            val directoryRelativePath =
                                directory.getFullPath().relativeTo(workspaceAbsolute)
                            val newNode = when (draggingItemPath) {
                                is FileTreeNode.Directory -> draggingItemPath.copy(
                                    parentRelativePath = directoryRelativePath,
                                    parentFullPath = directory.getFullPath()
                                )

                                is FileTreeNode.File -> draggingItemPath.copy(
                                    parentRelativePath = directoryRelativePath,
                                    parentFullPath = directory.getFullPath()
                                )
                            }

                            val result = workspaceSession.workspaceEnvStateFlow.value.renameNode(
                                oldNode = draggingItemPath,
                                newNode = newNode
                            )
                            result.fold(onFailure = {
                                Logger.e("result move: ${it}")
                            }, onSuccess = {
                                updateSyncStatus()
                            })

                        }
                    }
                }

                is Intent.OnFileTreeClick -> {
                    when (val data = intent.value.data) {
                        FileTreeItemPresentation.FileTreeItemPresentationData.Collections,
                        FileTreeItemPresentation.FileTreeItemPresentationData.Tags,
                        is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> {
                            val openedDirectories = state().openedDirectories.toMutableList()
                            if (openedDirectories.contains(intent.value.key)) {
                                openedDirectories.remove(intent.value.key)
                            } else {
                                openedDirectories.add(intent.value.key)
                            }
                            dispatch(SetOpenedDirectories(openedDirectories.toPersistentSet()))
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.File -> {
                            scope.launch {
                                resultBlock {
                                    val result =
                                        navigateToFileUseCase.invoke(NavigateToFile.File(data.fileNode))
                                    val timestamp = bind(result)
                                    navigator.navigate(Route.File(FilePageInput(timestamp)))
                                }
                            }
                        }

                        FileTreeItemPresentation.FileTreeItemPresentationData.Graph -> {
                            navigator.navigate(MainGraph)
                        }

                        FileTreeItemPresentation.FileTreeItemPresentationData.Home -> {
                            navigator.navigate(MainHome)
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.Tag -> {
                            navigator.navigate(Tag(TagPageInput(id = data.tag.id)))
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.Collection -> {
                            navigator.navigate(Collection(CollectionPageInput(collectionId = data.id)))
                        }
                    }
                }

                is Intent.SetOpenedDirectories -> {
                    dispatch(SetOpenedDirectories(intent.value))
                }

                is Intent.CreateFile -> {
                    when (val data = intent.directory.data) {
                        FileTreeItemPresentation.FileTreeItemPresentationData.Collections -> {
                            scope.launch {
                                val workspace = workspaceSession.workspaceEnvStateFlow.value
                                val collections =
                                    workspace.getCollectionsFlow().firstOrNull() ?: listOf()
                                val newCollectionName = findNewNameOrDefault(
                                    defaultName = "Untitled",
                                    existsNames = collections.map { d -> d.name }
                                )
                                workspace.createCollection(
                                    request = CreateCollectionRequest(
                                        name = newCollectionName,
                                        createdTime = nowInMs()
                                    )
                                )
                                updateSyncStatus()
                            }
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> {
                            scope.launch {
                                val parentFolder = data.fileNode
//                                    systemFilesManager.getFileNodeFromFullPath(
//                                    fullPath = data.fileNode.getFullPath(),
//                                    isDirectory = true
//                                )
                                workspaceSession.workspaceEnvStateFlow.value.createFileNode(
                                    parentFolder = parentFolder,
                                    fileName = FileName(
                                        name = "Untitled",
                                        extension = "md"
                                    ),
                                    isAbsoluteNew = true,
                                    byteArray = null
                                )
                                updateSyncStatus()
                            }
                        }

                        FileTreeItemPresentation.FileTreeItemPresentationData.Tags -> {
                            scope.launch {
                                val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                                val tags = workspaceEnv.getTagsFlow().firstOrNull() ?: listOf()
                                val newName = findNewNameOrDefault(
                                    defaultName = "Untitled",
                                    existsNames = tags.map { d -> d.name }
                                )
                                workspaceEnv.createTag(
                                    CreateTagRequest(
                                        name = newName,
                                        color = Color.Black,
                                        createdTime = nowInMs()
                                    )
                                )
                                updateSyncStatus()
                            }
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.File,
                        is FileTreeItemPresentation.FileTreeItemPresentationData.Collection,
                        FileTreeItemPresentation.FileTreeItemPresentationData.Graph,
                        FileTreeItemPresentation.FileTreeItemPresentationData.Home,
                        is FileTreeItemPresentation.FileTreeItemPresentationData.Tag -> doNothing()
                    }
                }

                is Intent.CreateDirectory -> {
                    when (val data = intent.directory.data) {
                        is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> {
                            val parentFolder = data.fileNode
//                                systemFilesManager.getFileNodeFromFullPath(
//                                fullPath = data.fileNode.getFullPath(),
//                                isDirectory = true
//                            )
                            scope.launch {
                                workspaceSession.workspaceEnvStateFlow.value.createDirectory(
                                    parentFolder = parentFolder,
                                    "Untitled",
                                    isAbsoluteNew = true
                                )
                            }
                            updateSyncStatus()
                        }

                        else -> doNothing()
                    }
                }

                is Intent.Rename -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    if (intent.newName.isEmpty()) {
                        dispatch(SetRenameFileNodeData(null))
                        return
                    }
                    when (val data = intent.directory.data) {
                        is FileTreeItemPresentation.FileTreeItemPresentationData.Collection -> {
                            scope.launch {
                                val newName = intent.newName
                                val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                                workspaceEnv.renameCollection(
                                    RenameDataCollectionRequest(
                                        id = data.id,
                                        newName = newName,
                                        modifiedTime = nowInMs()
                                    )
                                )
                            }
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.Directory -> {
                            scope.launch {
                                val fileNode = data.fileNode
                                val newNode = fileNode.copy(name = intent.newName)
                                val result = workspaceEnv.renameNode(
                                    fileNode,
                                    newNode
                                )
                                result.fold(onFailure = {
                                    messagerService.sendMessage(it)
                                }, onSuccess = {
                                    messagerService.sendMessage("успех")
                                })
                            }
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.File -> {
                            scope.launch {
                                val fileNode = systemFilesManager.getFileNodeFromFullPath(
                                    fullPath = data.fileNode.getFullPath(),
                                    isDirectory = false
                                )
                                if (fileNode is FileTreeNode.File) {
                                    val newNode = fileNode.copy(
                                        name = fileNode.name.copy(name = intent.newName)
                                    )
                                    workspaceEnv.renameNode(
                                        fileNode,
                                        newNode
                                    )
                                }
                            }
                        }

                        is FileTreeItemPresentation.FileTreeItemPresentationData.Tag -> {
                            workspaceEnv.renameTag(
                                request = RenameTagRequest(
                                    id = data.tag.id,
                                    newName = intent.newName,
                                    modifiedTime = nowInMs()
                                )
                            )
                        }

                        FileTreeItemPresentation.FileTreeItemPresentationData.Collections,
                        FileTreeItemPresentation.FileTreeItemPresentationData.Graph,
                        FileTreeItemPresentation.FileTreeItemPresentationData.Home,
                        FileTreeItemPresentation.FileTreeItemPresentationData.Tags -> doNothing()
                    }
                    dispatch(SetRenameFileNodeData(null))
                    updateSyncStatus()
                }

                is Intent.SetSettings -> {
                    scope.launch {
                        workspaceSession.setSettings(intent.data)
                    }
                }

                is Intent.Sync -> {
                    scope.launch {
                        dispatch(WorkspaceStore.Msg.SetSyncStatus(UIState.Loading))
                        val sd = syncUseCase.invoke(workspaceSession.workspaceEnvStateFlow.value)
                        dispatch(WorkspaceStore.Msg.SetSyncStatus(sd.mapToUIState {
                            it.message ?: ""
                        }))
                        when (sd) {
                            is ResultWrapper.Error -> {

                            }

                            is ResultWrapper.Success -> {
//                                syncUseCase.getStatus(workspaceSession.workspaceEnvStateFlow.value)

                                workspaceSession.initConfigAndFiles()

                                workspaceSession.loadLocalFont()

//                                updateSyncStatus()
                            }
                        }
                    }
                }

                is Intent.ClearingTabs -> {
                    val sizes =
                        state().tabSizes.filter { d -> intent.data.contains(d.key) }.toMutableMap()
                    for (newTab in intent.data) {
                        if (!sizes.contains(newTab)) {
                            sizes[newTab] = 1f
                        }
                    }
                    dispatch(WorkspaceStore.Msg.SetTabSizes(sizes.toImmutableMap()))
                }

                is Intent.RevealFile -> {
                    scope.launch {
                        when (val pageType = intent.data) {
                            PageNameData.PageType.Home -> publish(ScrollToIndex(0))

                            PageNameData.PageType.Graph -> publish(ScrollToIndex(1))

                            is PageNameData.PageType.FileNode -> {
                                send("file: ${pageType.fileTreeNode.getFullPath()}")
                            }

                            is PageNameData.PageType.Collection -> {
                                send("collection: ${pageType.id}")
                            }

                            is PageNameData.PageType.Tag -> {
                                send("tag: ${pageType.id}")
                            }

                            is PageNameData.PageType.CollectionRow -> {}

                            PageNameData.PageType.Tags -> {
                                send("tags_tab")
                            }
                        }
                    }
                }

                is Intent.OnOffsetTabChangeOffset -> {
                    scope.launch(Dispatchers.Main.immediate) {
                        val state = state()
                        val lockedMenuCovered = state.lockedMenuCovered
                        if (lockedMenuCovered != null) {
                            if (lockedMenuCovered.menu == MenuCoveredId) {
                                val newSize = state.menuWidth + (intent.data)
                                dispatch(WorkspaceStore.Msg.SetMenuWidth(newSize))
                            } else {
                                val draggedTabIndex = lockedMenuCovered.menu
                                val filledSizes = mutableMapOf<Int, Float>()
                                for (item in intent.allTabIndexes) {
                                    val value = state.tabSizes[item]
                                    filledSizes[item] = value ?: 1f
                                }
                                val newSizes = recalculateTabWeights(
                                    screenWidth = intent.screenWidth,
                                    draggedTabIndex = draggedTabIndex,
                                    dragMovementInWidth = intent.data,
                                    minWidthOfTab = 100f,
                                    tabWeights = filledSizes,
                                    grabPositionRatio = 1f
                                )
                                dispatch(SetTabSizes(newSizes.toImmutableMap()))
                            }
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, WorkspaceStore.Msg> {
        override fun State.reduce(msg: WorkspaceStore.Msg): State {
            return when (msg) {
                is SetRenameFileNodeData -> copy(renameFileNodeData = msg.value)
                is SetActiveTab -> copy(activeTabsIndex = msg.value)
                is SetContextMenu -> copy(contextMenuData = msg.value)
                is SetActiveWorkspace -> copy(activeWorkspace = msg.value)
                is SetDatabaseStatus -> copy(databaseStatus = msg.value)
                is SetFileNodes -> copy(files = msg.value)
                is SetCurrentTabData -> copy(activeTabPageData = msg.value)
                is SetOpenedDirectories -> copy(openedDirectories = msg.value)
                is SetIsFullMenu -> copy(isMenuOpened = msg.value)
                is SetTabSizes -> copy(tabSizes = msg.value)
                is SetMenuCovered -> copy(menuCovered = msg.value)
                is SetLockedMenuCovered -> copy(lockedMenuCovered = msg.value)
                is SetMenuWidth -> copy(menuWidth = msg.value)
                is SetPosition -> copy(cursorPosition = msg.value)
                is SetWorkspaceFont -> copy(workspaceFont = msg.value)
                is SetWorkspaceSettings -> copy(settings = msg.value)
                is SetIndexFiles -> copy(indexes = msg.value)
                is SetSyncStatus -> copy(syncStatus = msg.value)
            }
        }
    }
}