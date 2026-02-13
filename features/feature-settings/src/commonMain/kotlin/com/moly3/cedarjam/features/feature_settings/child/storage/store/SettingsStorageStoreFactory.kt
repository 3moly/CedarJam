package com.moly3.cedarjam.features.feature_settings.child.storage.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.navigation.BaseExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import com.moly3.cedarjam.features.feature_settings.child.storage.Intent
import com.moly3.cedarjam.features.feature_settings.child.storage.State
import kotlinx.collections.immutable.toPersistentList

internal class SettingsStorageStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val workspaceSession: WorkspaceSession,
    private val back: () -> Unit,
    private val close: () -> Unit
) : KoinComponent {


    fun create(): SettingsStorageStore = object : SettingsStorageStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = SettingsStorageStore::class.simpleName,
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, SettingsStorageStore.Msg, Unit>(lifecycle) {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                workspaceSession.tagLinksFlow.collectLatest {
                    dispatch(SettingsStorageStore.Msg.SetTagToFilesCount(it.size))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.tagToTagsFlow.collectLatest {
                    dispatch(SettingsStorageStore.Msg.SetTagToTagsCount(it.size))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.annotationsFlow.collectLatest {
                    dispatch(SettingsStorageStore.Msg.SetAnnotationsCount(it.size))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.collectionRowsFlow.collectLatest {
                    dispatch(SettingsStorageStore.Msg.SetRowsCount(it.size))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.collectionRowsFlow.collectLatest {
                    dispatch(SettingsStorageStore.Msg.SetRowsCount(it.size))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.tagsFlow.collectLatest {
                    dispatch(SettingsStorageStore.Msg.SetTagsCount(it.size))
                }
            }
            scopeFromStartToStop.launch {
                workspaceSession.filesFlow.collectLatest {
                    val stAll = it.map {
                        val allFiles = it.getAll(isSkipOwnNode = true)
                        allFiles.toPersistentList()
                    }
                    val st = it.map {
                        val allFiles = it.getAllFilesByExtension(extension = null)
                        allFiles.toPersistentList()
                    }
                    dispatch(SettingsStorageStore.Msg.SetFilesState(st))
                    dispatch(SettingsStorageStore.Msg.SetAllFilesState(stAll))
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Back -> back()
                Intent.Close -> close()
            }
        }
    }

    private object ReducerImpl : Reducer<State, SettingsStorageStore.Msg> {
        override fun State.reduce(msg: SettingsStorageStore.Msg): State {
            return when (msg) {
                is SettingsStorageStore.Msg.SetAllFilesState -> copy(allFilesState = msg.value)
                is SettingsStorageStore.Msg.SetFilesState -> copy(filesState = msg.value)
                is SettingsStorageStore.Msg.SetTagsCount -> copy(tagsCount = msg.value)
                is SettingsStorageStore.Msg.SetAnnotationsCount -> copy(annotationsCount = msg.value)
                is SettingsStorageStore.Msg.SetCollectionsCount -> copy(collectionsCount = msg.value)
                is SettingsStorageStore.Msg.SetRowsCount -> copy(rowsCount = msg.value)
                is SettingsStorageStore.Msg.SetTagToFilesCount -> copy(tagToFilesCount = msg.value)
                is SettingsStorageStore.Msg.SetTagToRowsCount -> copy(tagToRowsCount = msg.value)
                is SettingsStorageStore.Msg.SetTagToTagsCount -> copy(tagToTagsCount = msg.value)
            }
        }
    }
}