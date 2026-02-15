package com.moly3.cedarjam.pages.page_tab

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.IDecomposeScopeComponent
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.navigation.componentScope
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.navigation.stateFlow
import com.moly3.cedarjam.pages.page_collection.CollectionComponentImpl
import com.moly3.cedarjam.pages.page_collection_row.CollectionRowComponentImpl
import com.moly3.cedarjam.pages.page_file.FileComponentImpl
import com.moly3.cedarjam.pages.page_graph.GraphComponentImpl
import com.moly3.cedarjam.pages.page_home.HomeComponentImpl
import com.moly3.cedarjam.ui.pages.tag.TagComponentImpl
import com.moly3.cedarjam.ui.pages.tags.TagsComponentImpl
import com.moly3.cedarjam.core.domain.func.doNothing
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.graph
import com.moly3.cedarjam.ui.home
import com.moly3.cedarjam.ui.tags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TabComponentImpl(
    private val workspaceSession: WorkspaceSession,
    context: ComponentContext,
    storeFactory: StoreFactory,
    private val openMenu: (Boolean) -> Unit,
    private val tabIndex: Int
) : KoinComponent,
    ComponentContext by context,
    IDecomposeScopeComponent,
    TabComponent {

    private val coroutineScope: CoroutineScope by inject()

    val navigation = StackNavigation<Config>()
    private val _stateFlow = MutableStateFlow<State>(State())
    private val router = context.childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Empty(index = 0),
        childFactory = { g, childContext ->
            when (g) {
                is Config.Empty -> TabComponent.Child.Home(
                    HomeComponentImpl(
                        componentContext = childContext,
                        storeFactory = storeFactory,
                        workspaceSession = workspaceSession
                    )
                )

                is Config.CollRow -> TabComponent.Child.CollectionRow(
                    CollectionRowComponentImpl(
                        componentContext = childContext,
                        storeFactory = storeFactory,
                        data = g.data,
                        workspaceSession = workspaceSession
                    )
                )

                is Config.Collection -> TabComponent.Child.Collection(
                    CollectionComponentImpl(
                        componentContext = childContext,
                        storeFactory = storeFactory,
                        data = g.data,
                        workspaceSession = workspaceSession
                    )
                )

                is Config.File -> TabComponent.Child.File(
                    FileComponentImpl(
                        componentContext = childContext,
                        storeFactory = storeFactory,
                        openMenu = openMenu,
                        data = g.data,
                        workspaceSession = workspaceSession
                    )
                )

                is Config.Graph -> TabComponent.Child.Graph(
                    GraphComponentImpl(
                        componentContext = childContext,
                        storeFactory = storeFactory,
                        workspaceSession = workspaceSession
                    )
                )

                is Config.Tag -> TabComponent.Child.Tag(
                    TagComponentImpl(
                        componentContext = childContext,
                        storeFactory = storeFactory,
                        data = g.data,
                        workspaceSession = workspaceSession
                    )
                )

                is Config.Tags -> TabComponent.Child.Tags(
                    TagsComponentImpl(
                        componentContext = childContext,
                        storeFactory = storeFactory,
                        workspaceSession = workspaceSession
                    )
                )
            }
        }
    )


    init {

        router.subscribe {
            val activeIndex = it.active.configuration.index
            val indexes = router.value.items.map { d -> d.configuration.index }
            val minIndex = indexes.minOfOrNull { b -> b }
            val maxIndex = indexes.maxOfOrNull { b -> b }
            var canGoBack = false
            var canGoForward = false

            if (minIndex == null || maxIndex == null) {

            } else {
                if (activeIndex > minIndex) {
                    canGoBack = true
                }
                if (activeIndex < maxIndex) {
                    canGoForward = true
                }
            }
            _stateFlow.value = State(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    override val scope by componentScope()

    override val childStack: Value<ChildStack<*, TabComponent.Child>>
        get() = router

    @OptIn(ExperimentalCoroutinesApi::class)
    override val nameFlow: Flow<PageNameData?> = router.stateFlow.flatMapLatest { childStack ->
        val nameStateFlow = when (val instance = childStack.active.instance) {
            is TabComponent.Child.Collection -> instance.component.nameFlow
            is TabComponent.Child.CollectionRow -> instance.component.nameFlow
            is TabComponent.Child.File -> instance.component.nameFlow
            is TabComponent.Child.Graph -> flowOf(
                PageNameData(
                    name = CJText.Res(Res.string.graph),
                    pageType = PageNameData.PageType.Graph,
                    modifiedTime = null
                )
            )

            is TabComponent.Child.Home -> flowOf(
                PageNameData(
                    name = CJText.Res(Res.string.home),
                    pageType = PageNameData.PageType.Home,
                    modifiedTime = null
                )
            )

            is TabComponent.Child.Tag -> instance.component.nameFlow
            is TabComponent.Child.Tags -> flowOf(
                PageNameData(
                    name =CJText.Res(Res.string.tags),
                    pageType = PageNameData.PageType.Tags,
                    modifiedTime = null
                )
            )
        }
        nameStateFlow
    }


    @OptIn(DelicateDecomposeApi::class)
    override fun onNavigate(route: Route) {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            when (route) {
                Route.Back -> back()
                Route.Forward -> forward()
                else -> {
                    val nextIndex = router.value.active.configuration.index + 1

                    val config = when (route) {
                        is Route.CollRow -> Config.CollRow(nextIndex, route.data)
                        is Route.Collection -> Config.Collection(nextIndex, route.data)
                        is Route.File -> {
                            Config.File(nextIndex, route.data)
                        }
                        Route.MainGraph -> Config.Graph(nextIndex)
                        Route.MainHome -> Config.Empty(nextIndex)
                        is Route.Tag -> {

                            Config.Tag(nextIndex, route.data)
                        }

                        Route.Tags -> Config.Tags(nextIndex)

                        Route.Back -> TODO()
                        Route.Forward -> TODO()
                        is Route.Workspace -> TODO()
                        Route.Empty -> TODO()
                    }
                    openNext(config)
                }
            }
        }
    }

    private val _labels = MutableSharedFlow<Label>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State>
        get() = _stateFlow

    private fun back() {
        val activeIndex = router.value.active.configuration.index
        val previousIndex = router.value.items.map { d -> d.configuration.index }
            .filter { d -> activeIndex > d }
            .maxOfOrNull { z -> z }
        val config = router.value.items.firstOrNull { d -> d.configuration.index == previousIndex }

        if (config != null) {
            navigation.pushToFront(config.configuration)
        }
    }

    private fun openNext(config: Config) {
        val activeIndex = router.value.active.configuration.index
        val currentConfig = router.value.active.configuration
        val isSame = when (config) {
            is Config.CollRow -> when (currentConfig) {
                is Config.CollRow -> currentConfig.data == config.data
                else -> false
            }

            is Config.Collection -> when (currentConfig) {
                is Config.Collection -> currentConfig.data == config.data
                else -> false
            }

            is Config.Empty -> currentConfig is Config.Empty
            is Config.File -> when (currentConfig) {
                is Config.File -> currentConfig.data == config.data
                else -> false
            }

            is Config.Graph -> currentConfig is Config.Graph
            is Config.Tag -> when (currentConfig) {
                is Config.Tag -> currentConfig.data.id == config.data.id
                else -> false
            }

            is Config.Tags -> currentConfig is Config.Tags
        }
        if (isSame) {
            return
        }
        try {
            if (config.index > activeIndex) {
                navigation.navigate { currentStack ->
                    // Remove all items after activeIndex and add new config
                    currentStack
                        .sortedBy { d -> d.index }
                        .takeWhile { it.index <= activeIndex }
                        .plus(config)
                }
            }

        } catch (exc: Exception) {
            val indexes = router.value.items.map { d -> d.configuration.index }
                .sortedBy { b -> b }
            co.touchlab.kermit.Logger.e {
                "error navigate - allIndexes: ${indexes}. new config: ${config.index}"
            }
        }
    }

    private fun forward() {
        val activeIndex = router.value.active.configuration.index
        val previousIndex = router.value.items.map { d -> d.configuration.index }
            .filter { d -> activeIndex < d }
            .minOfOrNull { z -> z }
        val config = router.value.items.firstOrNull { d -> d.configuration.index == previousIndex }

        if (config != null) {
            navigation.pushToFront(config.configuration)
        }
    }

    override fun onIntent(intent: Intent) {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            when (intent) {
                Intent.Back -> back()
                Intent.Forward -> forward()
                is Intent.Rename -> {
                    val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                    try {
                        when (val pageType = intent.pageType) {
                            is PageNameData.PageType.Collection -> {
                                workspaceEnv.renameCollection(
                                    RenameDataCollectionRequest(
                                        id = pageType.id,
                                        newName = intent.newName,
                                        modifiedTime = nowInMs()
                                    )
                                )
                            }

                            is PageNameData.PageType.CollectionRow -> {
                                workspaceEnv.renameCollectionRow(
                                    RenameDataCollectionRowRequest(
                                        rowId = pageType.id,
                                        newName = intent.newName,
                                        modifiedTime = nowInMs()
                                    )
                                )
                            }

                            is PageNameData.PageType.FileNode -> {
                                val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

                                val fileNode = pageType.fileTreeNode
                                val newNode = when (fileNode) {
                                    is FileTreeNode.Directory -> fileNode.copy(name = intent.newName)
                                    is FileTreeNode.File -> fileNode.copy(
                                        name = fileNode.name.copy(name = intent.newName)
                                    )
                                }
                                workspaceEnv.renameNode(
                                    fileNode,
                                    newNode
                                )
                            }

                            is PageNameData.PageType.Tag -> {
                                workspaceEnv.renameTag(
                                    RenameTagRequest(
                                        id = intent.pageType.id,
                                        newName = intent.newName,
                                        modifiedTime = nowInMs()
                                    )
                                )
                            }

                            PageNameData.PageType.Graph,
                            PageNameData.PageType.Home,
                            PageNameData.PageType.Tags -> doNothing()
                        }

                    } catch (exc: IllegalArgumentException) {
                        _labels.emit(Label.ReturnOriginalName(intent.oldName))
                    }
                }
            }
        }
    }

    override val labels: Flow<Label> = _labels

    @Serializable
    sealed interface Config {

        val index: Int

        @Serializable
        data class Empty(override val index: Int) : Config

        @Serializable
        data class Tags(override val index: Int) : Config

        @Serializable
        data class Tag(override val index: Int, val data: TagPageInput) : Config

        @Serializable
        data class Graph(override val index: Int) : Config

        @Serializable
        data class File(override val index: Int, val data: FilePageInput) : Config

        @Serializable
        data class Collection(override val index: Int, val data: CollectionPageInput) : Config

        @Serializable
        data class CollRow(override val index: Int, val data: CollectionRowPageInput) : Config
    }
}