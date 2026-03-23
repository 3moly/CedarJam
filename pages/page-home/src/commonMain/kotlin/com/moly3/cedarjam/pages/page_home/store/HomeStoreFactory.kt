package com.moly3.cedarjam.pages.page_home.store

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.func.combine
import com.moly3.cedarjam.core.domain.func.ignoreSearchByRelativePath
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.model.LineMatch
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TimeMachine
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.service.WorkspaceSession.Companion.workspaceName
import com.moly3.cedarjam.core.domain.usecase.INavigateToFileUseCase
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.navigation.Route.CollRow
import com.moly3.cedarjam.navigation.Route.Collection
import com.moly3.cedarjam.navigation.Route.Tag
import com.moly3.cedarjam.navigation.Route.Tags
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.navigation.subToLog
import com.moly3.cedarjam.pages.page_home.Intent
import com.moly3.cedarjam.pages.page_home.State
import com.moly3.cedarjam.pages.page_home.State.Companion.fromSaveable
import com.moly3.cedarjam.pages.page_home.State.Companion.toSaveable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

internal class HomeStoreFactory(
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
    private val openWorkspaceSettings: (Boolean) -> Unit
) : KoinComponent {

    init {
        Logger.w { "HomeStoreFactory created ${workspaceSession.workspaceName()}" }
    }

    private val fileManagerService: FileManagerService by lazy {
        workspaceSession.fileManagerService
    }
    private val navigator: Navigator by inject()
    private val filesRepository: IFilesRepository by inject()

    private val navigateToFileUseCase: INavigateToFileUseCase by inject {
        parametersOf(fileManagerService)
    }

    fun create(stateKeeper: StateKeeper, lifecycle: Lifecycle): HomeStore = object : HomeStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = HomeStore::class.simpleName,
            initialState = stateKeeper.consumeOrDefault(
                "HomeStoreState",
                State.SaveableState.serializer(),
                default = State.SaveableState()
            ).fromSaveable(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(lifecycle) },
            reducer = ReducerImpl
        ) {}.also {
        stateKeeper.register(key = "HomeStoreState", strategy = State.SaveableState.serializer()) {
            it.state.toSaveable()
        }
    }

    private inner class ExecutorImpl(private val lifecycle: Lifecycle) :
        BaseExecutor<Intent, Unit, State, HomeStore.Msg, Unit>(lifecycle) {

        private val _searchTextState = MutableStateFlow("")

        private val collectionsFlow: Flow<ImmutableList<TimeMachine>> =
            kotlinx.coroutines.flow.combine(
                workspaceSession.collectionsFlow,
                _searchTextState
            ) { collections, search ->
                Pair(collections, search)
            }.map {
                val searchText = it.second.lowercase()
                val isSearch = it.second.isNotEmpty()

                val timeMachines = mutableListOf<TimeMachine>()
                for (collection in it.first) {
                    if (isSearch && !collection.name.lowercase()
                            .contains(searchText)
                    ) continue
                    timeMachines.add(
                        TimeMachine.Collection(
                            collection = collection,
                            modifiedTime = collection.modifiedTime
                        )
                    )
                }
                timeMachines.toPersistentList()
            }.shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1
            )

        private val tagsFlow: Flow<ImmutableList<TimeMachine>> =
            kotlinx.coroutines.flow.combine(
                workspaceSession.tagsFlow,
                _searchTextState
            ) { tags, search ->
                Pair(tags, search)
            }.map {
                val searchText = it.second.lowercase()
                val isSearch = it.second.isNotEmpty()

                val timeMachines = mutableListOf<TimeMachine>()
                for (tag in it.first) {
                    if (isSearch && !tag.name.lowercase()
                            .contains(searchText)
                    ) continue
                    timeMachines.add(
                        TimeMachine.Tag(
                            tag = tag,
                            modifiedTime = tag.modifiedTime
                        )
                    )
                }
                timeMachines.toPersistentList()
            }.shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1
            )

        private val rowsFlow: Flow<ImmutableList<TimeMachine>> =
            kotlinx.coroutines.flow.combine(
                workspaceSession.collectionRowsFlow,
                _searchTextState
            ) { rows, search ->
                Pair(rows, search)
            }.map {
                val searchText = it.second.lowercase()
                val isSearch = it.second.isNotEmpty()

                val timeMachines = mutableListOf<TimeMachine>()
                for (row in it.first) {
                    if (isSearch && !row.name.lowercase()
                            .contains(searchText)
                    ) continue
                    timeMachines.add(
                        TimeMachine.Row(
                            row = row,
                            modifiedTime = row.modifiedTime
                        )
                    )
                }
                timeMachines.toPersistentList()
            }.shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1
            )

        private val annotationsFlow: Flow<ImmutableList<TimeMachine>> =
            kotlinx.coroutines.flow.combine(
                workspaceSession.annotationsFlow,
                _searchTextState
            ) { rows, search ->
                Pair(rows, search)
            }.map {
                val searchText = it.second.lowercase()
                val isSearch = it.second.isNotEmpty()

                val timeMachines = mutableListOf<TimeMachine>()
                for (item in it.first) {
                    if (isSearch && !item.description.lowercase()
                            .contains(searchText)
                    ) continue
                    timeMachines.add(
                        TimeMachine.Annotation(
                            annotation = item,
                            modifiedTime = item.modifiedTime
                        )
                    )
                }
                timeMachines.toPersistentList()
            }.shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1
            )

        private val filesFlow: Flow<ImmutableList<TimeMachine>> =
            kotlinx.coroutines.flow.combine(
                workspaceSession.filesFlow,
                _searchTextState
            ) { rows, search ->
                Pair(rows, search)
            }.map {
                val searchText = it.second.lowercase()
                val isSearch = it.second.isNotEmpty()

                val timeMachines = mutableListOf<TimeMachine>()
                when (val state = it.first) {
                    is UIState.Error<*> -> {}
                    is UIState.Loading -> {}
                    is UIState.Success -> {
                        for (file in state.data.getAllFilesByExtension(null)) {
                            val matches = mutableListOf<LineMatch>()
                            if (isSearch) {

                                val isTitleContains = file.getFullName().lowercase()
                                    .contains(searchText)
                                val isText = file.name.extension == "txt" ||
                                        file.name.extension == "md"
                                val isTextContains = if (isText) {
                                    val textResult = filesRepository.getNodeText(file)
                                    when (textResult) {
                                        is ResultWrapper.Error -> false
                                        is ResultWrapper.Success -> {
                                            // Find matching lines
                                            textResult.value.lines()
                                                .forEachIndexed { index, line ->
                                                    if (line.lowercase()
                                                            .contains(searchText)
                                                    ) {
                                                        // Option 1: Add to simple string list
                                                        matches.add(
                                                            LineMatch(
                                                                line = index,
                                                                text = line
                                                            )
                                                        )

                                                        // Option 2: Add to structured list
//                                                        structuredMatches.add(
//                                                            SearchMatch(
//                                                                lineNumber = index + 1,
//                                                                content = line,
//                                                                type = MatchType.CONTENT
//                                                            )
//                                                        )
                                                    }
                                                }
                                            matches.isNotEmpty()
                                        }
                                    }
                                } else false

                                if (isTitleContains || isTextContains) {

                                } else {
                                    continue
                                }
                            }
                            val relativePath = file.getRelativePath()
                            if (ignoreSearchByRelativePath.any { d -> relativePath.contains(d) }) {
                                continue
                            }
                            timeMachines.add(
                                TimeMachine.FileNode(
                                    file = file,
                                    modifiedTime = file.modifiedTime,
                                    matches = if (matches.count() > 0) matches.toPersistentList() else null
                                )
                            )
                        }
                    }
                }
                timeMachines.toPersistentList()
            }.shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1
            )


        override fun executeAction(action: Unit) {
            super.executeAction(action)
//            lifecycle.subToLog("HomeStoreState ${workspaceSession.workspaceEnvStateFlow.value.getWorkspace().name}")
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)
            _searchTextState.value = state().searchTextFieldValue.text

            scopeFromStartToStop.launch {
                combine(
                    collectionsFlow,
                    tagsFlow,
                    rowsFlow,
                    filesFlow,
                    annotationsFlow
                ) { collections, tags, rows, files, annotations ->
                    val timeMachines = mutableListOf<TimeMachine>()
                    timeMachines.addAll(collections)
                    timeMachines.addAll(tags)
                    timeMachines.addAll(rows)
                    timeMachines.addAll(files)
                    timeMachines.addAll(annotations)

                    UIState.Success(
                        timeMachines
                            .sortedByDescending { d -> d.modifiedTime }
                            .toPersistentList())
                }.flowOn(io).collectLatest { searchData ->

                    dispatch(HomeStore.Msg.SetTimes(searchData))
                }
            }
        }

        private fun openFileNode(node: FileTreeNode) {
            scope.launch {
                resultBlock {
                    val route =
                        navigateToFileUseCase.invoke(NavigateToFile.RelativePath(node.getRelativePath()))
                    val timestamp = bind(route)
                    navigator.navigate(Route.File(FilePageInput(timestamp)))
                }
            }
        }

        private fun openRow(row: CollectionRowDTO) {
            navigator.navigate(
                CollRow(
                    CollectionRowPageInput(
                        collectionId = row.collectionId,
                        rowId = row.id
                    )
                )
            )
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.OpenTimeMachine -> {
                    when (val tm = intent.timeMachine) {
                        is TimeMachine.Collection -> navigator.navigate(
                            Collection(
                                CollectionPageInput(tm.collection.id)
                            )
                        )

                        is TimeMachine.FileNode -> openFileNode(tm.file)
                        is TimeMachine.Row -> openRow(tm.row)

                        is TimeMachine.Tag -> navigator.navigate(Tag(TagPageInput(tm.tag.id)))
                        is TimeMachine.Annotation -> {

                        }
                    }
                }

                is Intent.OpenWorkspaceSettings -> {
                    openWorkspaceSettings(true)
                }

                is Intent.OpenTimeMachineType -> {
                    dispatch(HomeStore.Msg.SetFilterType(intent.value))
                }

                Intent.OpenTags -> {
                    navigator.navigate(Tags)
                }

                is Intent.SetSearchText -> {
                    dispatch(HomeStore.Msg.SetSearchTextFieldValue(intent.value))
                    scope.launch {
                        _searchTextState.emit(intent.value.text)
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, HomeStore.Msg> {
        override fun State.reduce(msg: HomeStore.Msg): State {
            return when (msg) {
                is HomeStore.Msg.SetTimes -> copy(timeMachinesState = msg.value)
                is HomeStore.Msg.SetSearchTextFieldValue -> copy(searchTextFieldValue = msg.value)
                is HomeStore.Msg.SetAllNodesState -> copy(allNodes = msg.value)
                is HomeStore.Msg.SetFilterType -> copy(filterType = msg.value)
            }
        }
    }
}