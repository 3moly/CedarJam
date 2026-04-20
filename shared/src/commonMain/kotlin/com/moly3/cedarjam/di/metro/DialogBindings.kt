package com.moly3.cedarjam.di.metro

import com.moly3.cedarjam.core.domain.dialog.*
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
@ContributesTo(AppScope::class)
@BindingContainer
object DialogBindings {
    @Provides fun provideDialogRegistry(): DialogRegistry = DialogRegistry()
    @Provides fun provideIDialogRegister(registry: DialogRegistry): IDialogRegister = registry
    @Provides fun provideDialogSelectTagService(register: IDialogRegister):DialogSelectTagService = DialogSelectTagService(register)
    @Provides fun provideDialogTagToTagService(register: IDialogRegister):DialogTagToTagService = DialogTagToTagService(register)
    @Provides fun provideDialogDeleteService(register: IDialogRegister):DialogDeleteService = DialogDeleteService(register)
    @Provides fun provideDialogCreateWorkspaceService(register: IDialogRegister):DialogCreateWorkspaceService = DialogCreateWorkspaceService(register)
    @Provides fun provideDialogAddCollectionRowService(register: IDialogRegister):DialogAddCollectionRowService = DialogAddCollectionRowService(register)
    @Provides fun provideDialogSelectOptionsService(register: IDialogRegister):DialogSelectOptionsService = DialogSelectOptionsService(register)
    @Provides fun provideDialogColorPickerService(register: IDialogRegister):DialogColorPickerService = DialogColorPickerService(register)
    @Provides fun provideDialogSelectWorkspaceService(register: IDialogRegister):DialogSelectWorkspaceService = DialogSelectWorkspaceService(register)
}

