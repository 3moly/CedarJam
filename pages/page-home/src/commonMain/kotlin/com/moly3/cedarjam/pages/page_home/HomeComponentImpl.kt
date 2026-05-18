package com.moly3.cedarjam.pages.page_home

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCaseFactory
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.pages.page_home.store.HomeStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class HomeComponentImpl(
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    @Assisted private val workspaceSession: WorkspaceSession,
    @Assisted private val openWorkspaceSettings: (Boolean) -> Unit,
    private val navigator: Navigator,
    private val filesRepository: IFilesRepository,
    private val navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory,
) : HomeComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        HomeStoreFactory(
            workspaceSession = workspaceSession,
            storeFactory = storeFactory,
            openWorkspaceSettings = openWorkspaceSettings,
            navigator = navigator,
            filesRepository = filesRepository,
            navigateToFileUseCaseFactory = navigateToFileUseCaseFactory,
        ).create(stateKeeper = stateKeeper, lifecycle = lifecycle)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }

    override val workspaceFullPath: String = workspaceSession.workspaceEnvStateFlow.value.getWorkspace().absolutePath
}
