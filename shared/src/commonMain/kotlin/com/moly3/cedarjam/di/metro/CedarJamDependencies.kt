package com.moly3.cedarjam.di.metro

import com.moly3.cedarjam.core.coordinator.SetIsDarkCoordinator
import com.moly3.cedarjam.core.domain.dialog.DialogAddCollectionRowService
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectOptionsService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.repository.IAnkiEnvironment
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.AlertService
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.domain.service.IFileHasher
import com.moly3.cedarjam.core.domain.service.IImageTransform
import com.moly3.cedarjam.core.domain.service.IMessageService
import com.moly3.cedarjam.core.domain.service.IUtilsService
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.domain.usecase.NavigateToFileUseCaseFactory
import com.moly3.cedarjam.core.domain.usecase.OpenNodeDataUseCaseFactory
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.core.data.di.AppGraphServices
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.NavigatorDispatcher
import dev.zacsweers.metro.Inject

@Inject
class CedarJamDependencies(
    override val dialogRegistry: DialogRegistry,
    override val appEnvironment: IAppEnvironment,
    override val navigatorDispatcher: NavigatorDispatcher,
    override val openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory,
    override val dialogSelectTagService: DialogSelectTagService,
    override val dialogTagToTagService: DialogTagToTagService,
    override val dialogSelectOptionsService: DialogSelectOptionsService,
    override val dialogCreateWorkspaceService: DialogCreateWorkspaceService,
    override val dialogAddCollectionRowService: DialogAddCollectionRowService,
    override val dialogSelectWorkspaceService: DialogSelectWorkspaceService,
    override val utilsService: IUtilsService,
    override val ankiEnvironment: IAnkiEnvironment,
    override val macTrackpadGestureService: MacTrackpadGestureService,
    override val imageTransform: IImageTransform,
    override val jvmBrowserService: IJvmBrowserService,
    override val appContextProvider: AppContextProvider,
    override val fileHasher: IFileHasher,
    override val filesRepository: IFilesRepository,
    override val setIsDarkCoordinator: SetIsDarkCoordinator,
    override val alertService: AlertService,
    override val syncUseCase: ISyncUseCase,
    override val messageService: IMessageService,
    override val dialogColorPickerService: DialogColorPickerService,
    override val dialogDeleteService: DialogDeleteService,
    override val navigator: Navigator,
    override val navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory,
    override val remote: IRemoteSyncRepository,
) : AppGraphServices
