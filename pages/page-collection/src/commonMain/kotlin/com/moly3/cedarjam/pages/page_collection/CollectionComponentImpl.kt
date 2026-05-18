package com.moly3.cedarjam.pages.page_collection

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectOptionsService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.repository.IAnkiEnvironment
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCaseFactory
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.pages.page_collection.store.CollectionStoreFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class CollectionComponentImpl(
    @Assisted workspaceSession: WorkspaceSession,
    @Assisted componentContext: ComponentContext,
    @Assisted storeFactory: StoreFactory,
    @Assisted private val data: CollectionPageInput,
    @Assisted private val openWorkspaceSettings: (Boolean) -> Unit,
    private val utilsService: IUtilsService,
    private val dialogSelectTagService: DialogSelectTagService,
    private val dialogSelectOptionsService: DialogSelectOptionsService,
    private val dialogDeleteService: DialogDeleteService,
    private val navigator: Navigator,
    private val ankiEnvironment: IAnkiEnvironment,
    private val navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory,
) : CollectionComponent,
    ComponentContext by componentContext {

    private val store by lazy {
        CollectionStoreFactory(
            storeFactory = storeFactory,
            lifecycle = lifecycle,
            pageData = data,
            workspaceSession = workspaceSession,
            openWorkspaceSettings = openWorkspaceSettings,
            utilsService = utilsService,
            dialogSelectTagService = dialogSelectTagService,
            dialogSelectOptionsService = dialogSelectOptionsService,
            dialogDeleteService = dialogDeleteService,
            navigator = navigator,
            ankiEnv = ankiEnvironment,
            navigateToFileUseCaseFactory = navigateToFileUseCaseFactory,
        ).create()
    }

    override val nameFlow: StateFlow<PageNameData?> = store.nameStateFlow

    override val labels = store.labels

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow
    override fun onIntent(intent: Intent) {
        store.accept(intent)
    }
}
