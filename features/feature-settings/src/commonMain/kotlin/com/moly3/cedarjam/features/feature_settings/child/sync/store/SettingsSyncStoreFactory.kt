package com.moly3.cedarjam.features.feature_settings.child.sync.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.func.getRelativePath
import com.moly3.cedarjam.core.domain.func.isNotExact
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAll
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.mapToUIState
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.navigation.BaseExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import com.moly3.cedarjam.features.feature_settings.child.sync.Intent
import com.moly3.cedarjam.features.feature_settings.child.sync.State
import com.moly3.cedarjam.features.feature_settings.child.sync.model.FileVersionLine
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.inject
import kotlin.getValue

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
            val workspace = env.getWorkspace()
            scope.launch(io) {
                try {
                    val getLocalFiles = env.getNodes(null).getAll(isSkipOwnNode = true)
                    val getServerFiles = env.getServerFiles()
                    getServerFiles.shouldBeSuccess()

                    val versionsFiles = mutableListOf<FileVersionLine>()
                    for (item in getServerFiles.value.files) {

                        if (!item.isDirectory) {
                            val foundLocalItem = getLocalFiles.firstOrNull { d ->
                                val localRelativePath =
                                    d.getRelativePath(workspacePath = workspace.absolutePath)
                                val soResult =
                                    localRelativePath == item.relativePath.normalizeText()
                                soResult
                            }
                            if (foundLocalItem == null || foundLocalItem.modifiedTime.isNotExact(item.modifiedTime)) {
                                versionsFiles.add(
                                    FileVersionLine(
                                        fileRelativePath = item.relativePath.normalizeText(),
                                        currentTime = foundLocalItem?.modifiedTime,
                                        serverTime = item.modifiedTime
                                    )
                                )
                            }
                        }
                    }
                    for (localNode in getLocalFiles) {
                        if (localNode.isDirectory())
                            continue

                        val fileRelativePath =
                            localNode.getRelativePath(workspacePath = workspace.absolutePath)
                        val foundVirtFile =
                            getServerFiles.value.files.firstOrNull { s -> fileRelativePath == s.relativePath.normalizeText() }
                        if (foundVirtFile == null) {
                            versionsFiles.add(
                                FileVersionLine(
                                    fileRelativePath = fileRelativePath,
                                    currentTime = localNode.modifiedTime,
                                    serverTime = null
                                )
                            )
                        }
                    }
                    val state = UIState.Success(versionsFiles.toPersistentList())
                    launch(Dispatchers.Main) {
                        dispatch(SettingsSyncStore.Msg.SetFilesVersions(state))
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
                is SettingsSyncStore.Msg.SetFilesVersions -> copy(fileVersionsState = msg.value)
                is SettingsSyncStore.Msg.SetUploadState -> copy(uploadState = msg.value)
            }
        }
    }
}