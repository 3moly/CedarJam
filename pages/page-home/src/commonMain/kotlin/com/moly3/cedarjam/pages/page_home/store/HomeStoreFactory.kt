package com.moly3.cedarjam.pages.page_home.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.Route.CollRow
import com.moly3.cedarjam.navigation.Route.Collection
import com.moly3.cedarjam.navigation.Route.Tag
import com.moly3.cedarjam.navigation.Route.Tags
import com.moly3.cedarjam.navigation.consumeOrDefault
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.navigation.subToLog
import com.moly3.cedarjam.pages.page_home.Intent
import com.moly3.cedarjam.pages.page_home.State
import com.moly3.cedarjam.pages.page_home.State.Companion.fromSaveable
import com.moly3.cedarjam.pages.page_home.State.Companion.toSaveable
import com.moly3.cedarjam.pages.page_home.model.LineMatch
import com.moly3.cedarjam.pages.page_home.model.TimeMachine
import com.moly3.cedarjam.core.domain.func.combine
import com.moly3.cedarjam.core.domain.func.getRelativePath
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.mapToUIState
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.INavigateToFileUseCase
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.pages.page_home.store.HomeStore.Msg
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.collections.forEachIndexed

internal class HomeStoreFactory(
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
) : KoinComponent {

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

    data class SearchData(
        val collections: List<CollectionDTO>,
        val tags: List<TagDTO>,
        val rows: List<CollectionRowDTO>,
        val filesState: UIState<List<FileTreeNode>, String>,
        val searchText: String
    )

    private inner class ExecutorImpl(private val lifecycle: Lifecycle) :
        BaseExecutor<Intent, Unit, State, HomeStore.Msg, Unit>(lifecycle) {

        private val _searchTextState = MutableStateFlow("")


        override fun executeAction(action: Unit) {
            super.executeAction(action)

            lifecycle.subToLog("HomeStoreState")
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)
            _searchTextState.value = state().searchTextFieldValue.text

            lifecycle.doOnResume {
                try {
                    val allNodes =
                        workspaceSession.workspaceEnvStateFlow.value.getNodes(null).getAll(true)
                    dispatch(Msg.SetAllNodesState(UIState.Success(allNodes.toPersistentList())))
                } catch (exc: Exception) {
                }
            }
            scopeFromStartToStop.launch {
                combine(
                    workspaceSession.collectionsFlow,
                    workspaceSession.tagsFlow,
                    workspaceSession.collectionRowsFlow,
                    workspaceSession.filesFlow,
                    _searchTextState
                ) { collections, tags, rows, filesState, searchText ->
//                    Logger.e("HomeStoreFactory")
                    SearchData(collections, tags, rows, filesState, searchText)
                }.flatMapLatest { searchData ->

                    val searchText = searchData.searchText.lowercase()
                    val isSearch = searchData.searchText.isNotEmpty()
                    flow {
                        val timeMachines = mutableListOf<TimeMachine>()
                        for (collection in searchData.collections) {
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
                        when (val state = searchData.filesState) {
                            is UIState.Error<*> -> {}
                            is UIState.Loading -> {}
                            is UIState.Success -> {
                                for (file in state.data.getAllFilesByExtension(null)) {
                                    val matches = mutableListOf<LineMatch>()
                                    if (isSearch) {

                                        val isTitleContains = file.getShortName().lowercase()
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
                        for (tag in searchData.tags) {
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
                        for (row in searchData.rows) {
                            if (isSearch && !row.name.lowercase()
                                    .contains(searchData.searchText.lowercase())
                            ) continue
                            timeMachines.add(
                                TimeMachine.Row(
                                    row = row,
                                    modifiedTime = row.modifiedTime
                                )
                            )
                        }
                        emit(
                            UIState.Success(
                                timeMachines
                                    .sortedByDescending { d -> d.modifiedTime }
                                    .toPersistentList()))
                    }
                }
                    .flowOn(io)
                    .collectLatest {
                        dispatch(HomeStore.Msg.SetTimes(it))
                    }
            }
            scopeFromStartToStop.launch {
                while (this.isActive) {
                    delay(1_00)
                    dispatch(HomeStore.Msg.SetCount(state().count + 1))
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {

                is Intent.OpenFileNode -> {
                    scope.launch {
                        resultBlock {
                            val route =
                                navigateToFileUseCase.invoke(NavigateToFile.RelativePath(intent.fullPath))
                            val timestamp = bind(route)
                            navigator.navigate(Route.File(FilePageInput(timestamp)))
                        }
                    }
                }

                is Intent.OpenCollection -> {
                    navigator.navigate(Collection(CollectionPageInput(intent.id)))
                }

                is Intent.OpenRow -> {
                    navigator.navigate(
                        CollRow(
                            CollectionRowPageInput(
                                collectionId = intent.collectionId,
                                rowId = intent.id
                            )
                        )
                    )
                }

                is Intent.OpenTag -> {
                    navigator.navigate(Tag(TagPageInput(intent.id)))
                }

                Intent.OpenTags -> {
                    navigator.navigate(Tags)
                }

                is Intent.SetSearchText -> {
                    scope.launch {
                        _searchTextState.emit(intent.value.text)
                        dispatch(HomeStore.Msg.SetSearchTextFieldValue(intent.value))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, HomeStore.Msg> {
        override fun State.reduce(msg: HomeStore.Msg): State {
            return when (msg) {
                is HomeStore.Msg.SetCount -> copy(count = msg.value)
                is HomeStore.Msg.SetTimes -> copy(timeMachinesState = msg.value)
                is HomeStore.Msg.SetSearchTextFieldValue -> copy(searchTextFieldValue = msg.value)
                is HomeStore.Msg.SetAllNodesState -> copy(allNodes = msg.value)
            }
        }
    }
}