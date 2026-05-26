package com.moly3.cedarjam.pages.page_collection.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectOptionsService
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.Route.CollRow
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.pages.page_collection.Intent
import com.moly3.cedarjam.pages.page_collection.Label
import com.moly3.cedarjam.pages.page_collection.State
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.dialog.model.DialogSelectOptionsServiceInput
import com.moly3.cedarjam.core.domain.dialog.model.SelectOption
import com.moly3.cedarjam.core.ui.func.getPdfResult
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.anki.AnkiNote
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.mapToUpdateRequest
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IAnkiEnvironment
import com.moly3.cedarjam.core.domain.repository.getCollectionRows
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.usecase.INavigateToFileUseCase
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCaseFactory
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import com.moly3.cedarjam.navigation.Route
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.time.ExperimentalTime


internal class CollectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val pageData: CollectionPageInput,
    private val workspaceSession: WorkspaceSession,
    private val openWorkspaceSettings: (Boolean) -> Unit,
    private val utilsService: IUtilsService,
    private val dialogSelectTagService: DialogSelectTagService,
    private val dialogSelectOptionsService: DialogSelectOptionsService,
    private val dialogDeleteService: DialogDeleteService,
    private val navigator: Navigator,
    private val ankiEnv: IAnkiEnvironment,
    private val navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory,
) {

    private val currentPageState = MutableStateFlow(0)
    private val pageSizeState = MutableStateFlow(10L)
    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val navigateToFileUseCase: INavigateToFileUseCase
        get() = navigateToFileUseCaseFactory(workspaceSession.fileManagerService)

    fun create(): CollectionStore = object : CollectionStore,
        Store<Intent, State, Label> by storeFactory.create(
            name = CollectionStore::class.simpleName,
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {
        override val nameStateFlow: StateFlow<PageNameData?>
            get() = workspaceSession.collectionsFlow.map {
                val collection = it.firstOrNull { b -> b.id == pageData.collectionId }
                if (collection == null) {
                    null
                } else {
                    PageNameData(
                        name = CJText.Raw(collection.name),
                        pageType = PageNameData.PageType.Collection(id = collection.id),
                        modifiedTime = collection.modifiedTime
                    )
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = null
            )
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private val rowsCountFlow: Flow<Long> =
        workspaceSession.workspaceEnvStateFlow
            .flatMapLatest { workspaceEnv ->
                workspaceEnv
                    .getCollectionRowsCount(pageData.collectionId)
            }
            .distinctUntilChanged()
            .flowOn(io)

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, CollectionStore.Msg, Label>(lifecycle) {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                workspaceSession.tagsFlow.collectLatest {
                    dispatch(CollectionStore.Msg.SetTags(it.toPersistentList()))
                }
            }
            scopeFromStartToStop.launch {
                currentPageState.collectLatest {
                    dispatch(CollectionStore.Msg.SetCurrentPage(it.toLong() + 1))
                }
            }
            scopeFromStartToStop.launch {
                combine(
                    rowsCountFlow,
                    pageSizeState
                ) { rowsCount, pageSize ->
                    if (pageSize <= 0) {
                        0
                    } else {
                        val s = ceil(rowsCount.toDouble() / pageSize.toDouble()).toInt()
                        if (s == 0)
                            1
                        else
                            s
                    }
                }.distinctUntilChanged()
                    .collectLatest { maxPage ->
                        dispatch(CollectionStore.Msg.SetMaxPage(maxPage.toLong()))
                    }
            }
            scopeFromStartToStop.launch {
                combine(
                    currentPageState,
                    workspaceSession.workspaceEnvStateFlow,
                    pageSizeState
                ) { currentPage, workspaceEnv, pageSize ->
                    Triple(currentPage, workspaceEnv, pageSize)
                }
                    .flatMapLatest { (currentPage, workspaceEnv, pageSize) ->
                        val offset = currentPage.toLong() * pageSize

                        workspaceEnv.getCollectionRowsPaginated(
                            offset = offset,
                            pageSize = pageSize,
                            collectionId = pageData.collectionId
                        )
                    }
                    .flowOn(io)
                    .collectLatest { rows ->
                        dispatch(CollectionStore.Msg.SetRows(rows))
                    }
            }
            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow
                    .collectLatest {
                        dispatch(CollectionStore.Msg.SetWorkspace(it.getWorkspace()))
                    }
            }
            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow
                    .flatMapLatest {
                        it.getTagCollectionRowsFlow()
                    }.collectLatest {
                        dispatch(CollectionStore.Msg.SetTagCollectionRows(it))
                    }
            }
            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow
                    .flatMapLatest {
                        it.getCollectionFlow(collectionId = pageData.collectionId)
                    }.collectLatest {
                        if (state().collection?.name != it?.name) {
                            publish(Label.ReturnOriginalName(it?.name ?: ""))
                        }
                        dispatch(CollectionStore.Msg.SetCollection(it))
                    }
            }
        }

        @OptIn(ExperimentalTime::class)
        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.OpenOptions -> {
                    scope.launch {
                        val result =
                            dialogSelectOptionsService.open(
                                DialogSelectOptionsServiceInput(
                                    options = persistentListOf(
                                        SelectOption(
                                            text = "delete",
                                            onClick = {
                                                scope.launch {
                                                    dialogDeleteService.open(Unit)
                                                }
                                            }
                                        )
                                    )
                                ))
                    }
                }

                is Intent.OpenWorkspaceSettings -> {
                    openWorkspaceSettings(true)
                }

                is Intent.DeleteCollectionRow -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        workspaceEnv.deleteCollectionRow(intent.id)
                    }
                }

                is Intent.OpenCollectionRow -> {
                    scope.launch {
                        navigator.navigate(
                            CollRow(
                                CollectionRowPageInput(
                                    collectionId = intent.collectionId,
                                    rowId = intent.rowId
                                )
                            )
                        )
                    }
                }

                is Intent.RenameCollectionRow -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        workspaceEnv.renameCollectionRow(
                            request = RenameDataCollectionRowRequest(
                                rowId = intent.oldData.id,
                                newName = intent.newName,
                                modifiedTime = nowInMs()
                            )
                        )
                    }
                }

                is Intent.ImportToAnki -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

                        val collectionName = state().collection?.name ?: return@launch
                        val deckName = "CedarJam::${collectionName}"
                        val rows =
                            workspaceEnv.getCollectionRows(collectionId = pageData.collectionId)
                        val ankiNotes = rows.distinctBy { d -> d.name }.map {
                            AnkiNote(
                                id = null,
                                deckName = deckName,
                                modelName = "CedarJam-basic-source",
                                fields = mapOf(
                                    "Front" to it.name,
                                    "Back" to it.translation.toString(),
                                    "Source" to "Kotlin Documentation"
                                ),
                                tags = listOf("kotlin", "programming")
                            )
                        }
                        ankiEnv.importNotes(deckName = deckName, notes = ankiNotes)
                    }
                }

//                is Intent.Generate -> {
//                    scope.launch {
//                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
//                        val name = "rowwww"
//                        for (item in 0 until 500000) {
//                            workspaceEnv.createCollectionRow(
//                                CreateCollectionRowRequest(
//                                    name = "$name$item",
//                                    collectionId = pageData.collectionId,
//                                    createdTime = nowInMs()
//                                )
//                            )
//                        }
//                    }
//                }

//                is Intent.AddCollectionRow -> {
//                    scope.launch {
//                        val workspaceEnv =   sessionStorage.workspaceEnv.value
//
//                        val collectionRow = dialogAddCollectionRowService.open(Unit)
//                        if (collectionRow != null && workspaceEnv != null) {
//                            workspaceEnv.createCollectionRow(
//                                request = CreateCollectionRowRequest(
//                                    name = collectionRow.name,
//                                    collectionId = pageData.collectionId,
//                                    fileRelativePath = collectionRow.fileRelativePath,
//                                    imgRelativePath = collectionRow.imgRelativePath,
//                                    webLink = collectionRow.webLink,
//                                    currentProgress = collectionRow.currentProgress,
//                                    progressMax = collectionRow.progressMax,
//                                    isCompleted = collectionRow.isCompleted,
//                                    translation = collectionRow.translation,
//                                    pronunciation = collectionRow.pronunciation,
//                                    exampleSentence = collectionRow.exampleSentence,
//                                    createdTime = nowInMs()
//                                )
//                            )
//                        }
//                    }
//                }

                is Intent.CreateCollectionRow -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    workspaceEnv.createRow(
                        request = CreateCollectionRowRequest(
                            name = intent.name,
                            collectionId = pageData.collectionId,
                            fileRelativePath = null,
                            imgRelativePath = null,
                            webLink = null,
                            currentProgress = null,
                            progressMax = null,
                            isCompleted = false,
                            translation = null,
                            pronunciation = null,
                            exampleSentence = null,
                            createdTime = nowInMs()
                        )
                    )
                }

                is Intent.ChangeViewType -> {
                    scope.launch {
                        val collection = state().collection
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        if (collection != null) {
                            workspaceEnv.updateCollection(
                                request = UpdateDataCollectionRequest(
                                    id = collection.id,
                                    viewType = intent.viewType,
                                    modifiedTime = nowInMs()
                                )
                            )
                        }
                    }
                }

                is Intent.OpenWebLink -> {
                    utilsService.openLink(intent.value)
                }

                is Intent.OpenDocument -> {
                    scope.launch {
                        resultBlock {
                            val result =
                                navigateToFileUseCase.invoke(NavigateToFile.RelativePath(intent.value))
                            val timestamp = bind(result)
                            navigator.navigate(Route.File(FilePageInput(timestamp)))
                        }
                    }
                }

                is Intent.AddCollectionRowTag -> {
                    scope.launch {
                        try {
                            val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                            val tag = dialogSelectTagService.open(workspaceSession)
                            if (tag != null) {
                                workspaceEnv.createTagRow(
                                    request = CreateTagCollectionRowRequest(
                                        tagId = tag.id,
                                        rowId = intent.oldData.id,
                                        createdTime = nowInMs()
                                    )
                                )
                            }
                        } catch (exc: Exception) {
                        }
                    }
                }

                Intent.NextPage -> {
                    if (currentPageState.value < state().maxPage - 1)
                        currentPageState.value = currentPageState.value + 1
                }

                Intent.PreviousPage -> {
                    if (currentPageState.value > 0)
                        currentPageState.value = currentPageState.value - 1
                }

                is Intent.SetDocumentToRow -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

                        when (val data = intent.fileTreeNode.data) {
                            is FileTreeItemPresentation.FileTreeItemPresentationData.File -> {
                                var updateRequest = intent.row.copy(
                                    fileRelativePath = data.fileNode.getRelativePath(),
                                    modifiedTime = nowInMs()
                                ).mapToUpdateRequest()
                                if (data.fileNode.name.extension == "pdf") {
                                    val pdfReuslt = getPdfResult(data.fileNode.getFullPath())
                                    updateRequest =
                                        updateRequest.copy(progressMax = pdfReuslt.numberOfPages.toDouble())
                                }

                                workspaceEnv.updateCollectionRow(
                                    request = updateRequest
                                )
                            }

                            else -> {}
                        }
                    }
                }

                is Intent.RenameCollection -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    try {
                        workspaceEnv.renameCollection(
                            RenameDataCollectionRequest(
                                id = pageData.collectionId,
                                newName = intent.newName,
                                modifiedTime = nowInMs()
                            )
                        )
                    } catch (exc: IllegalArgumentException) {
                        val oldName = state().collection?.name ?: ""
                        println("RenameCollection ${exc.message}. oldName: ${oldName}")
                        this.publish(Label.ReturnOriginalName(oldName))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, CollectionStore.Msg> {
        override fun State.reduce(msg: CollectionStore.Msg): State {
            return when (msg) {
                is CollectionStore.Msg.SetRows -> copy(rows = msg.value.toPersistentList())
                is CollectionStore.Msg.SetCollection -> copy(collection = msg.value)
                is CollectionStore.Msg.SetWorkspace -> copy(workspace = msg.value)
                is CollectionStore.Msg.SetTagCollectionRows -> copy(tagCollectionRows = msg.value.toPersistentList())
                is CollectionStore.Msg.SetCurrentPage -> copy(currentPage = msg.value)
                is CollectionStore.Msg.SetMaxPage -> copy(maxPage = msg.value)
                is CollectionStore.Msg.SetTags -> copy(tags = msg.value)
            }
        }
    }
}