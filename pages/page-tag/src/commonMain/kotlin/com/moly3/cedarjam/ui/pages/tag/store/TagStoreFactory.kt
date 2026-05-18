package com.moly3.cedarjam.ui.pages.tag.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.ui.pages.tag.Intent
import com.moly3.cedarjam.ui.pages.tag.State
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.node.ObsidianGraphPresentation
import com.moly3.cedarjam.core.domain.model.node.toGraphData
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.IOpenNodeDataUseCase
import com.moly3.cedarjam.core.ui.model.CJText
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.mapper.toRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class TagStoreFactory(
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val pageData: TagPageInput,
    private val openWorkspaceSettings: (Boolean) -> Unit,
    private val selectTagService: DialogSelectTagService,
    private val openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    private val navigator: Navigator,
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val openNodeUseCase: IOpenNodeDataUseCase get() =
        openNodeDataUseCaseFactory(workspaceSession.fileManagerService)

    fun create(): TagStore = object : TagStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = TagStore::class.simpleName,
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {
        override val nameStateFlow: StateFlow<PageNameData?>
            get() = workspaceSession.tagsFlow.map {
                val tag = it.firstOrNull { b -> b.id == pageData.id }
                if (tag != null) {
                    PageNameData(
                        name = CJText.Raw(tag.name),
                        pageType = PageNameData.PageType.Tag(id = tag.id),
                        modifiedTime = tag.modifiedTime
                    )
                } else {
                    null
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = null
            )
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, TagStore.Msg, Unit>(lifecycle) {

        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                workspaceSession.tagsFlow.collect {
                    val tag = it.firstOrNull { d -> d.id == pageData.id }
                    if (tag != null) {
                        dispatch(TagStore.Msg.SetTagState(UIState.Success(tag)))
                    } else {
                        dispatch(TagStore.Msg.SetTagState(UIState.Error(Unit)))
                    }
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.SetNewTag -> {
                    scope.launch {
                        val tag = selectTagService.open(workspaceSession)
                        if (tag != null && tag.id != pageData.id) {
                            workspaceSession.workspaceEnvStateFlow.value.createTagToTag(
                                CreateTagToTagRequest(
                                    tagId = pageData.id,
                                    tag2Id = tag.id,
                                    createdTime = nowInMs()
                                )
                            )
                        }
                    }
                }

                is Intent.OpenWorkspaceSettings -> {
                    openWorkspaceSettings(true)
                }

                is Intent.OpenLink -> {
                    scope.launch {
                        resultBlock {
                            val result = openNodeUseCase.invoke(intent.data.toGraphData(), false)
                            navigator.navigate(bind(result).toRoute())
                        }
                    }
                }

                is Intent.DeleteLink -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

                        when (intent.data) {
                            is ObsidianGraphPresentation.Collection -> {

                            }

                            is ObsidianGraphPresentation.CollectionRow -> {
                                val links = workspaceEnv
                                    .getTagCollectionRowsFlow()
                                    .first()
                                    .filter { d -> d.tagId == pageData.id && d.rowId == intent.data.value.id }
                                for (link in links) {
                                    workspaceEnv.deleteTagCollectionRow(link.id)
                                }
                            }

                            is ObsidianGraphPresentation.File -> {

                            }

                            is ObsidianGraphPresentation.Tag -> {
                                val tagToTagLinks = workspaceEnv
                                    .getTagToTagsFlow()
                                    .first()
                                    .filter { d ->
                                        (d.firstTagId == pageData.id && d.secondTagId == intent.data.value.id) ||
                                                (d.secondTagId == pageData.id && d.firstTagId == intent.data.value.id)
                                    }
                                for (link in tagToTagLinks) {
                                    workspaceEnv.deleteTagToTag(link.id)
                                }
                            }

                            is ObsidianGraphPresentation.Unknown -> {}
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, TagStore.Msg> {
        override fun State.reduce(msg: TagStore.Msg): State {
            return when (msg) {
                is TagStore.Msg.SetTagState -> copy(tagState = msg.value)
                is TagStore.Msg.SetConnections -> copy(connections = msg.value)
            }
        }
    }
}