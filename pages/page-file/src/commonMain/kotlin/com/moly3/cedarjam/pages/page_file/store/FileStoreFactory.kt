package com.moly3.cedarjam.pages.page_file.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.getGraphId
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.request.CreateTagLinkRequest
import com.moly3.cedarjam.core.domain.model.toGetFileType
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.pages.page_file.Intent
import com.moly3.cedarjam.pages.page_file.State
import com.moly3.cedarjam.pages.page_file.State.Companion.fromSaveable
import com.moly3.cedarjam.pages.page_file.State.Companion.toSaveable
import com.moly3.cedarjam.pages.page_file.store.FileStore.Msg.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class FileStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val data: FilePageInput,
    private val workspaceSession: WorkspaceSession,
    private val setIsShowGraph: (String, Boolean) -> Unit,
    private val showCanvasDialog: (FileTreeNode.File) -> Unit
) : KoinComponent {

    private val fileManagerService: FileManagerService by lazy {
        workspaceSession.fileManagerService
    }
    private val navigator: Navigator by inject()
    private val appEnvironment: IAppEnvironment by inject()
    private val filesRepository: IFilesRepository by inject()

    private val foundNodeFlow = combine(
        workspaceSession.workspaceEnvStateFlow.value.getFileNodesFlow(),
        fileManagerService.fileNodeState
    ) { files, timestamp ->
        val keyVal = timestamp.states[data.timestamp]
        val nodes = files.getOrDefault(listOf()).getAllFilesByExtension(null)
        val found =
            nodes.firstOrNull { d -> d.getFullPath() == keyVal?.fileNodeFullPath }
        println("list: ${timestamp.states.size} ts: ${data.timestamp} is found - ${found?.name?.name}. or ${keyVal?.fileNodeFullPath}")
        found
    }

    fun create(stateKeeper: StateKeeper): FileStore = object : FileStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = FileStore::class.simpleName,
            initialState = stateKeeper.consumeOrDefault(
                "FileStore",
                State.SaveableState.serializer(),
                default = State.SaveableState()
            ).fromSaveable(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {

        override val nameStateFlow: Flow<PageNameData?> = foundNodeFlow.map {
            val file = it
            if (file != null) {
                println("nameStateFlow mapping file name: ${file.name.name}")
                PageNameData(
                    name = CJText.Raw(file.name.name),
                    pageType = PageNameData.PageType.FileNode(
                        timestamp = data.timestamp,
                        fileTreeNode = file
                    ),
                    modifiedTime = file.modifiedTime
                )
            } else {
                null
            }
        }
    }.also {
        stateKeeper.register(key = "FileStore", strategy = State.SaveableState.serializer()) {
            it.state.toSaveable()
        }
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, FileStore.Msg, Unit>(lifecycle) {

        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                foundNodeFlow.collectLatest {
                    val fileNodeId = it?.getGraphId()
                    if (fileNodeId != null) {
                        workspaceSession
                            .getConnectionPresentations(fileNodeId)
                            .collectLatest {
                                dispatch(FileStore.Msg.SetConnectionsCount(it.size))
                            }
                    }

                }
            }
            val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
            scopeFromStartToStop.launch {
                combine(
                    foundNodeFlow,
                    workspaceEnv.getAnnotationsFlow()
                ) { node, annotations ->
                    if (node != null) {
                        annotations
                            .filter { d -> d.dataPath == node.getRelativePath() }
                            .toPersistentList()
                    } else {
                        persistentListOf()
                    }
                }.collectLatest {
                    dispatch(FileStore.Msg.SetAnnotations(it))
                }
            }
            scopeFromStartToStop.launch {
                fileManagerService.closeDeletedFile.collectLatest {
                    if (data.timestamp == it) {
                        navigator.navigate(Route.MainHome)
                    }
                }
            }


            scopeFromStartToStop.launch {
                workspaceEnv.getTagLinksFlow().collectLatest {
                    dispatch(FileStore.Msg.SetTagLinks(it.toPersistentList()))
                }
            }
            scopeFromStartToStop.launch {
                workspaceEnv.getTagsFlow().collectLatest {
                    dispatch(FileStore.Msg.SetTags(it.toPersistentList()))
                }
            }
            scopeFromStartToStop.launch {
                foundNodeFlow.collectLatest { fileNode ->
                    if (fileNode != null) {
                        val rl = fileNode.getRelativePath()
                        dispatch(FileStore.Msg.SetFileRelativePath(rl))

                        val fileType = fileNode.toGetFileType(
                            filesRepository = filesRepository
                        )
                        when (fileType) {
                            is FileType.Canvas -> {
                                showCanvasDialog(fileType.fileNode)
                            }

                            is FileType.PDF -> {
                                val state = state()
                                if (state.fileType == null) {
                                    dispatch(
                                        FileStore.Msg.SetFile(
                                            FileType.PDF(fileType.fileNode, 1)
                                        )
                                    )
                                }
                            }

                            is FileType.Image,
                            is FileType.MIDI,
                            is FileType.Text,
                            FileType.Unknown,
                            is FileType.Video -> {
                                dispatch(
                                    FileStore.Msg.SetFile(
                                        fileType
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SetIsShowGraph -> {
                    scope.launch {
                        val file = foundNodeFlow.first()
                        if (file != null) {
                            val graphId = file.getGraphId()
                            setIsShowGraph(graphId, intent.value)
                        }
                    }
                }

                is Intent.ChangeTextNode -> {
                    scope.launch {
                        println("Intent.ChangeTextNode 1")
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        println("Intent.ChangeTextNode 2")
                        val oldFullPath =
                            fileManagerService.getFileNodeByTimestamp(timestamp = data.timestamp)
                        val nodes = workspaceEnv.getNodes(null).getAllFilesByExtension(null)
                        val found = nodes.firstOrNull { d -> d.getFullPath() == oldFullPath }
                        if (found != null) {
                            workspaceEnv.setNodeText(
                                found,
                                intent.newText
                            )
                        } else {

                        }
                        println("Intent.ChangeTextNode 3")
                    }
                }

//                is Intent.SaveCanvas -> {
//                    Logger.e {
//                        "start saving canvas"
//                    }
//                    canvasSaveJob?.cancel()
//                    canvasSaveJob = scope.launch {
//                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
//                        val fileNodeFullPath =
//                            fileManagerService.getFileNodeByTimestamp(timestamp = data.timestamp)
//                        delay(500L)
//                        val nodes = workspaceEnv.getNodes(null).getAllFilesByExtension(null)
//                        val fileNode =
//                            nodes.firstOrNull { d -> d.getFullPath() == fileNodeFullPath }
//
//                        try {
//                            if (fileNode != null) {
//                                val workspace = workspaceEnv.getWorkspace()
//                                val shapes = intent.shapes.map { d ->
//                                    MapCardJson(
//                                        id = d.id,
//                                        position = WorldPosition(0f, 0f),
////                                        position = d.position,
//                                        size = OffsetData(
//                                            d.size.x, d.size.y
//                                        ),
////                                        fileData = d.data?.toFileNodeJson(workspace),
//                                        fileData = null,
////                                        color = d.color?.toHexString()
//                                        color = null
//                                    )
//                                }
//                                val connections = intent.connections.map { d ->
//                                    ArcConnectionJson(
//                                        id = d.id,
//                                        fromBox = d.fromBox,
//                                        toBox = d.toBox,
//                                        fromSide = BoxSide.BOTTOM,
//                                        toSide = BoxSide.LEFT,
////                                        fromSide = d.fromSide,
////                                        toSide = d.toSide,
//                                        arcHeight = d.arcHeight,
//                                        color = d.color?.toHexString(),
//                                    )
//                                }
//
//                                val json =
//                                    DefaultJson.encodeToString(
//                                        CanvasData(
//                                            shapes = shapes,
//                                            connections = connections
//                                        )
//                                    )
//                                filesRepository.setNodeText(fileNode, json)
//                            }
//                        } catch (exc: Exception) {
//                            Logger.e {
//                                "error saving canvas: ${exc.message}"
//                            }
//                        }
//                    }
//                }

                is Intent.SetLinkTag -> {
                    val fullPath =
                        fileManagerService.getFileNodeByTimestamp(timestamp = data.timestamp)
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    if (fullPath != null) {
                        workspaceEnv.createTagLink(
                            request = CreateTagLinkRequest(
                                tagId = intent.value.id,
                                fullPath = fullPath
                            )
                        )
                    }
                }

                is Intent.RemoveLinkTag -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    workspaceEnv.deleteTagLink(id = intent.value.id)
                }

                is Intent.PageBack -> {
                    dispatch(SetFile(intent.file.copy(currentPage = intent.file.currentPage - 1)))
                }

                is Intent.PageNext -> {
                    dispatch(SetFile(intent.file.copy(currentPage = intent.file.currentPage + 1)))
                }

                is Intent.ToPage -> {
                    dispatch(SetFile(intent.file.copy(currentPage = intent.page)))
                }

                is Intent.AddAnnotation -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

                    workspaceEnv.createAnnotation(intent.value.copy(dataPath = state().relativePath))
                }

                is Intent.DeleteAnnotation -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    workspaceEnv.deleteAnnotation(id = intent.value.id)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, FileStore.Msg> {
        override fun State.reduce(msg: FileStore.Msg): State {
            return when (msg) {
                is SetFile -> copy(fileType = msg.value)
                is SetTags -> copy(tags = msg.value)
                is SetTagLinks -> copy(tagLinks = msg.value)
                is SetFileRelativePath -> copy(relativePath = msg.value)
                is SetConnectionsCount -> copy(connectionsCount = msg.value)
                is SetAnnotations -> copy(annotations = msg.value)
            }
        }
    }
}