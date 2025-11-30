package com.moly3.cedarjam.pages.page_select_workspace

import com.arkivanov.decompose.ComponentContext
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.IMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SelectWorkspaceComponent(
    componentContext: ComponentContext,
    private val onSelectWorkspace: (WorkspaceInput) -> Unit
) : ISelectWorkspaceComponent,
    ComponentContext by componentContext,
    KoinComponent {

    private val appEnvironment: IAppEnvironment by inject()
    private val dialogCreateWorkspaceService: DialogCreateWorkspaceService by inject()
    private val coroutineScope: CoroutineScope by inject()
    private val isd: IMessageService by inject()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: Flow<State> = appEnvironment.getWorkspacesFlow().map {
        State(
            workspacesState = it
        )
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.SelectWorkspace -> {
                onSelectWorkspace(WorkspaceInput(intent.workspace.name))
            }

            Intent.CreateWorkspace -> {
                coroutineScope.launch(io) {
                    val result = dialogCreateWorkspaceService.open(Unit)
                    if (result != null) {
                        val result = appEnvironment.createWorkspace(result)
                        when (result) {
                            is ResultWrapper.Error -> {
                                isd.sendMessage(result.error)
                            }

                            is ResultWrapper.Success -> {

                            }
                        }
                    }
                }
            }

            is Intent.DeleteWorkspace -> {
                coroutineScope.launch(io) {
                    appEnvironment.deleteWorkspace(intent.workspace)
                }
            }
        }
    }
}
