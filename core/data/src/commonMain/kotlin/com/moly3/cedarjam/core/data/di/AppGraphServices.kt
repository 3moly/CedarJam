package com.moly3.cedarjam.core.data.di

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
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry
import com.moly3.cedarjam.core.ui.service.IJvmBrowserService
import com.moly3.cedarjam.core.ui.service.MacTrackpadGestureService
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.NavigatorDispatcher

/**
 * Application service surface for UI/store layers that cannot depend on the `shared` Metro graph
 * (avoids Gradle cycles like shared → page-workspace → page-collection → shared).
 */
interface AppGraphServices {
    val dialogRegistry: DialogRegistry
    val appEnvironment: IAppEnvironment
    val navigatorDispatcher: NavigatorDispatcher
    val openNodeDataUseCaseFactory: OpenNodeDataUseCaseFactory
    val dialogSelectTagService: DialogSelectTagService
    val dialogTagToTagService: DialogTagToTagService
    val dialogSelectOptionsService: DialogSelectOptionsService
    val dialogCreateWorkspaceService: DialogCreateWorkspaceService
    val dialogAddCollectionRowService: DialogAddCollectionRowService
    val dialogSelectWorkspaceService: DialogSelectWorkspaceService
    val utilsService: IUtilsService
    val ankiEnvironment: IAnkiEnvironment
    val macTrackpadGestureService: MacTrackpadGestureService
    val imageTransform: IImageTransform
    val jvmBrowserService: IJvmBrowserService
    val appContextProvider: AppContextProvider
    val fileHasher: IFileHasher
    val filesRepository: IFilesRepository
    val setIsDarkCoordinator: SetIsDarkCoordinator
    val alertService: AlertService
    val syncUseCase: ISyncUseCase
    val messageService: IMessageService
    val dialogColorPickerService: DialogColorPickerService
    val dialogDeleteService: DialogDeleteService
    val navigator: Navigator
    val navigateToFileUseCaseFactory: NavigateToFileUseCaseFactory
    val remote: IRemoteSyncRepository
}

object AppGraphServicesLocator {
    lateinit var instance: AppGraphServices
}
