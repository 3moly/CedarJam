package com.moly3.cedarjam.pages.page_select_workspace

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.pages.page_select_workspace.store.SelectWorkspaceStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class SelectWorkspaceComponent(
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory, // Provided by DI Graph
    @Assisted private val onSelectWorkspace: (WorkspaceInput) -> Unit,
    private val appEnvironment: IAppEnvironment,
    private val messageService: IMessageService,
    private val dialogCreateWorkspaceService: DialogCreateWorkspaceService
) : ISelectWorkspaceComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        SelectWorkspaceStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            onSelectWorkspace = onSelectWorkspace,
            appEnvironment = appEnvironment,
            messageService = messageService,
            dialogCreateWorkspaceService = dialogCreateWorkspaceService
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow

    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
