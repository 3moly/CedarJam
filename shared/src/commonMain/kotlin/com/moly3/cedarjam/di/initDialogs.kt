package com.moly3.cedarjam.di

import com.moly3.cedarjam.core.domain.dialog.DialogAddCollectionRowService
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import org.koin.dsl.module

fun initDialogs() = module {
    single<DialogSelectTagService> { DialogSelectTagService() }
    single<DialogTagToTagService> { DialogTagToTagService() }
    single<DialogDeleteService> { DialogDeleteService() }
    single<DialogCreateWorkspaceService> { DialogCreateWorkspaceService() }
    single<DialogAddCollectionRowService> { DialogAddCollectionRowService() }
    single<DialogColorPickerService> { DialogColorPickerService() }
    single<DialogSelectWorkspaceService> { DialogSelectWorkspaceService() }
}