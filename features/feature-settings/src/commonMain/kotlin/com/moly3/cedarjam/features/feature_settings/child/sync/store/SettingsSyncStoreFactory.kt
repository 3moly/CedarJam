package com.moly3.cedarjam.features.feature_settings.child.sync.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.mapToUIState
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.features.feature_settings.child.sync.Intent
import com.moly3.cedarjam.features.feature_settings.child.sync.State
import com.moly3.cedarjam.navigation.BaseExecutor
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class SettingsSyncStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val workspaceSession: WorkspaceSession,
    private val back: () -> Unit,
    private val close: () -> Unit
) : KoinComponent {

    private val syncUseCase: ISyncUseCase by inject()

    fun create(): SettingsSyncStore = object : SettingsSyncStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = SettingsSyncStore::class.simpleName,
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, SettingsSyncStore.Msg, Unit>(lifecycle) {

        private fun refreshStatusFiles() {
            val env = workspaceSession.workspaceEnvStateFlow.value
            val deletedFiles = env.getDeletedFilesMetadata()
            dispatch(SettingsSyncStore.Msg.SetFileMetadata(deletedFiles.toPersistentMap()))
            scope.launch(io) {
                try {
                    val resultss =
                        syncUseCase.getStatus(workspace = workspaceSession.workspaceEnvStateFlow.value)
                    launch(Dispatchers.Main) {
                        dispatch(SettingsSyncStore.Msg.SetPrepareStatus(resultss.mapToUIState(onError = { "" })))
                    }
                } catch (exc: Exception) {
                    val msg = "" + exc.message
                }
            }
        }


        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                refreshStatusFiles()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Back -> back()
                Intent.Close -> close()
                Intent.Sync -> {
                    scope.launch {
                        val resultss =
                            syncUseCase.invoke(workspace = workspaceSession.workspaceEnvStateFlow.value)
                        val env = workspaceSession.workspaceEnvStateFlow.value

                        dispatch(SettingsSyncStore.Msg.SetUploadState(resultss.mapToUIState(onError = { "" })))

                        env.initConfigAndFiles()
                        workspaceSession.loadLocalFont()
                        env.reinitDatabase()
                        refreshStatusFiles()
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, SettingsSyncStore.Msg> {
        override fun State.reduce(msg: SettingsSyncStore.Msg): State {
            return when (msg) {
                is SettingsSyncStore.Msg.SetPrepareStatus -> copy(fileVersionsState = msg.value)
                is SettingsSyncStore.Msg.SetUploadState -> copy(uploadState = msg.value)
                is SettingsSyncStore.Msg.SetFileMetadata -> copy(deletedFiles = msg.value)
            }
        }
    }
}