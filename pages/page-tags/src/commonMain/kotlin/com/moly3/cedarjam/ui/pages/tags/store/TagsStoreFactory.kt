package com.moly3.cedarjam.ui.pages.tags.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.ui.pages.tags.Intent
import com.moly3.cedarjam.ui.pages.tags.State
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class TagsStoreFactory(
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val dialogTagToTagService: DialogTagToTagService,
) {

    fun create(stateKeeper: StateKeeper): TagsStore = object : TagsStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = TagsStore::class.simpleName,
            initialState = State(), //stateKeeper.consume("") ?:
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}.also {
//        stateKeeper.register(key = "CalculatorStoreState", strategy = State.seriali) {
//            it.state.copy(isLoading = false) // We can reset any transient state here
//        }
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, TagsStore.Msg, Unit>(lifecycle) {

        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                workspaceSession.tagToTagsFlow.collectLatest {
                    dispatch(TagsStore.Msg.SetTagToTags(it))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.tagsFlow.collectLatest {
                    dispatch(TagsStore.Msg.SetTags(it))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.tagLinksFlow.collectLatest {
                    dispatch(TagsStore.Msg.SetTagLinks(it))
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.DeleteTagToTag -> {
                    val workspace = workspaceSession.workspaceEnvStateFlow.value
                    workspace.deleteTagToTag(id = intent.id)
                }

                is Intent.CreateTag -> {
                    val workspace = workspaceSession.workspaceEnvStateFlow.value
                    workspace.createTag(intent.tag)
                }

                is Intent.RenameTag -> {
                    scope.launch {
                        val workspace = workspaceSession.workspaceEnvStateFlow.value
                        workspace.renameTag(
                            request = RenameTagRequest(
                                id = intent.tag.id,
                                newName = "scopaz${Random.nextInt()}",
                                modifiedTime = nowInMs()
                            )
                        )
                    }
                }

                Intent.AddTagToTag -> {
                    scope.launch {
                        val result = dialogTagToTagService.open(workspaceSession)
                        val workspace = workspaceSession.workspaceEnvStateFlow.value
                        if (result != null) {
                            workspace.createTagToTag(
                                CreateTagToTagRequest(
                                    tagId = result.firstTag.id,
                                    tag2Id = result.secondTag.id,
                                    createdTime = nowInMs()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, TagsStore.Msg> {
        override fun State.reduce(msg: TagsStore.Msg): State {
            return when (msg) {
                is TagsStore.Msg.SetTagToTags -> copy(tagToTags = msg.value)
                is TagsStore.Msg.SetTags -> copy(tags = msg.value)
                is TagsStore.Msg.SetTagLinks -> copy(tagLinks = msg.value)
            }
        }
    }
}