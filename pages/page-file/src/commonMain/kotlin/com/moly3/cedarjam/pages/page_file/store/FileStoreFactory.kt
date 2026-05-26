package com.moly3.cedarjam.pages.page_file.store

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.func.shareScope
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.request.CreateTagLinkRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateRowsByPdf
import com.moly3.cedarjam.core.domain.model.toGetFileType
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.IImageTransform
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.func.saveAsPng
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
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal class FileStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val data: FilePageInput,
    private val openMenu: (Boolean) -> Unit,
    private val workspaceSession: WorkspaceSession,
    private val showCanvasDialog: (FileTreeNode.File) -> Unit,
    private val navigator: Navigator,
    private val imageTransform: IImageTransform,
    private val filesRepository: IFilesRepository,
) {

    private val fileManagerService: FileManagerService by lazy {
        workspaceSession.fileManagerService
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val foundNodeFlow = combine(
        workspaceSession.filesFlow,
        fileManagerService.fileNodeState
    ) { files, timestamp ->
        val keyVal = timestamp.states[data.timestamp]
        val nodes = files.getOrDefault(listOf()).getAllFilesByExtension(null)
        val found = nodes.firstOrNull { d -> d.getRelativePath() == keyVal?.fileNodeRelativePath }
        found
    }.distinctUntilChangedBy { file -> file?.getRelativePath() }.shareScope(scope = scope)

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
        }.shareScope(scope = scope)
    }.also {
        stateKeeper.register(key = "FileStore", strategy = State.SaveableState.serializer()) {
            it.state.toSaveable()
        }
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, FileStore.Msg, Unit>(lifecycle) {

        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
            scopeFromStartToStop.launch {
                combine(
                    foundNodeFlow,
                    workspaceEnv.getAnnotationsFlow()
                ) { node, annotations ->
                    if (node != null) {
                        val nodePath = node.getRelativePath()
                        annotations
                            .filter { d ->
                                d.dataPath.normalizeText() == nodePath.normalizeText()
                            }
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
                workspaceEnv.getTagFilesFlow().collectLatest {
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
                        val pdfInputData = data.type as? FilePageInput.FilePageType.Pdf
                        when (fileType) {
                            is FileType.Canvas -> {
                                showCanvasDialog(fileType.fileNode)
                            }

                            is FileType.PDF -> {
                                val state = state()
                                if (state.fileType == null) {
                                    dispatch(
                                        FileStore.Msg.SetFile(
                                            FileType.PDF(fileType.fileNode, pdfInputData?.page ?: 1)
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

        private fun navigatePdf(file: FileType.PDF, newPage: Int) {
            scope.launch {
                workspaceSession.workspaceEnvStateFlow.value.updateRowsForPdf(
                    UpdateRowsByPdf(
                        relativePath = file.fileNode.getRelativePath(),
                        newPage = newPage
                    )
                )
            }
            dispatch(SetFile(file.copy(currentPage = newPage)))
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.ChangeTextNode -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        val oldFullPath =
                            fileManagerService.getFileNodeByTimestamp(timestamp = data.timestamp)
                        val nodes = workspaceEnv.getNodes(null).getAllFilesByExtension(null)
                        val found = nodes.firstOrNull { d -> d.getRelativePath() == oldFullPath }
                        if (found != null) {
                            workspaceEnv.setNodeText(
                                found,
                                intent.newText
                            )
                        }
                    }
                }

                is Intent.SetLinkTag -> {
                    val fullPath =
                        fileManagerService.getFileNodeByTimestamp(timestamp = data.timestamp)
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    if (fullPath != null) {
                        workspaceEnv.createTagLink(
                            request = CreateTagLinkRequest(
                                tagId = intent.value.id,
                                relativePath = fullPath
                            )
                        )
                    }
                }

                is Intent.RemoveLinkTag -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    workspaceEnv.deleteTagLink(id = intent.value.id)
                }

                is Intent.PageBack -> {
                    val newPage = intent.file.currentPage - 1
                    navigatePdf(intent.file, newPage)

                }

                is Intent.PageNext -> {
                    val newPage = intent.file.currentPage + 1
                    navigatePdf(intent.file, newPage)
                }

                is Intent.ToPage -> {
                    dispatch(SetFile(intent.file.copy(currentPage = intent.page)))
                }

                is Intent.AddAnnotation -> {
                    val fileRelativePath = state().relativePath
                    scope.launch(io) {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        val connectedRows =
                            workspaceEnv.getCollectionRowsFlowByFileRelativePath(relativePath = fileRelativePath)
                                .firstOrNull()

                        val annotationRequest = intent.value.copy(dataPath = fileRelativePath)

                        val fullPath = pathWrapper(
                            workspaceEnv.getWorkspace().absolutePath,
                            fileRelativePath
                        ).pathString

                        //TODO if connectedRows > 1, ask user to choose
                        val rowId = connectedRows?.firstOrNull()?.id
                        val result =
                            workspaceEnv.createAnnotation(annotationRequest.copy(rowId = rowId))
                        when (result) {
                            is ResultWrapper.Error -> {
                                Logger.w { "annotation adding error: ${result.error}" }
                            }

                            is ResultWrapper.Success -> {
                                val cropCache = pathWrapper(
                                    workspaceEnv.getWorkspace().absolutePath,
                                    hiddenDirectory,
                                    "image_cache",
                                    "annotation_${result.value}.png"
                                ).pathString
                                try {
                                    val image = imageTransform.getPdfImage(
                                        path = fullPath,
                                        page = (annotationRequest.dataPoint.toInt() - 1),
                                        density = intent.density
                                    )
                                    val cropped = imageTransform.cropNormalized(
                                        image,
                                        x = annotationRequest.x,
                                        y = annotationRequest.y,
                                        width = annotationRequest.width,
                                        height = annotationRequest.height
                                    )
                                    cropped.saveAsPng(cropCache)
                                } catch (exc: Exception) {

                                }
                            }
                        }
                    }
                }

                is Intent.DeleteAnnotation -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    workspaceEnv.deleteAnnotation(id = intent.value.id)
                }

                is Intent.OpenWorkspaceSettings -> {
                    openMenu(true)
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