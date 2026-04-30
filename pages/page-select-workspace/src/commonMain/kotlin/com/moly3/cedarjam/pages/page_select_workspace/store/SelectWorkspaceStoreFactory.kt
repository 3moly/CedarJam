package com.moly3.cedarjam.pages.page_select_workspace.store

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.mapToUIState
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.pages.page_select_workspace.Intent
import com.moly3.cedarjam.pages.page_select_workspace.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.SupervisorJob

internal class SelectWorkspaceStoreFactory(
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val appEnvironment: IAppEnvironment,
    private val isd: IMessageService,
    private  val dialogCreateWorkspaceService: DialogCreateWorkspaceService,
    private val onSelectWorkspace: (WorkspaceInput) -> Unit
) {
//    private val dialogCreateWorkspaceService: DialogCreateWorkspaceService get() = d.dialogCreateWorkspaceService
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
//    private val isd: IMessageService get() = d.messageService

    fun create(): SelectWorkspaceStore = object : SelectWorkspaceStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = SelectWorkspaceStore::class.simpleName,
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, SelectWorkspaceStore.Msg, Unit>(lifecycle) {

        private fun refreshLocalWorkspaces() {
            scope.launch {
                dispatch(
                    SelectWorkspaceStore.Msg.SetLocalWorkspaces(
                        appEnvironment.getLocalWorkspaces().mapToUIState(
                            mapError = { "" },
                            mapSuccess = { d -> d },
                            onError = { "" })
                    )
                )
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            lifecycle.doOnResume {
                scope.launch {
                    val serverWorkspacesResult = appEnvironment.getServerWorkspaces().mapToUIState { "" }
                    dispatch(SelectWorkspaceStore.Msg.SetServerWorkspaces(serverWorkspacesResult))
                    //en
                }
                refreshLocalWorkspaces()
                scope.launch {
                    appEnvironment.getWorkspacesFlow().collectLatest {
                        dispatch(SelectWorkspaceStore.Msg.SetWorkspaces(it))
                    }
                }
            }
        }

        private suspend fun createWork(workspace: Workspace){
            val result = appEnvironment.createWorkspace(workspace)
            when (result) {
                is ResultWrapper.Error -> {
                    isd.sendMessage(result.error)
                }

                is ResultWrapper.Success -> {
                    if(result.value.name.isEmpty()){
                        throw NullPointerException("workspace is result.value.name empty")
                    }
                    withContext(Dispatchers.Main) {
                        onSelectWorkspace(
                            result.value
                        )
                    }
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SelectWorkspace -> {
                    onSelectWorkspace(
                        WorkspaceInput(
                            intent.workspace.name,
                            intent.workspace.serverName
                        )
                    )
                }

                Intent.CreateWorkspace -> {
                    coroutineScope.launch(io) {
                        val result = dialogCreateWorkspaceService.open(Unit)
                        if (result != null) {
                            createWork(result)
                        }
                    }
                }

                is Intent.DeleteWorkspace -> {
                    coroutineScope.launch(io) {
                        appEnvironment.deleteWorkspace(intent.workspace)
                        refreshLocalWorkspaces()
                    }
                }

                is Intent.FastCreate -> {
                    scope.launch {
                        createWork(Workspace(name = intent.name, serverName = intent.name, platformPath = intent.name))
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, SelectWorkspaceStore.Msg> {
        override fun State.reduce(msg: SelectWorkspaceStore.Msg): State {
            return when (msg) {
                is SelectWorkspaceStore.Msg.SetWorkspaces -> copy(workspacesState = msg.value)
                is SelectWorkspaceStore.Msg.SetLocalWorkspaces -> copy(localWorkspacesState = msg.value)
                is SelectWorkspaceStore.Msg.SetServerWorkspaces -> copy(serverWorkspacesState = msg.value)
            }
        }
    }
}