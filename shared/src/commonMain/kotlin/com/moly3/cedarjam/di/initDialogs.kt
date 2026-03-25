package com.moly3.cedarjam.di

import com.moly3.cedarjam.core.domain.dialog.DialogAddCollectionRowService
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectOptionsService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectTagService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.dialog.IDialogRegister
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry
import org.koin.dsl.bind
import org.koin.dsl.module

fun initDialogs() = module {
    factory { DialogSelectTagService(get()) }
    factory { DialogTagToTagService(get()) }
    factory { DialogDeleteService(get()) }
    factory { DialogCreateWorkspaceService(get()) }
    factory { DialogAddCollectionRowService(get()) }
    factory { DialogSelectOptionsService(get()) }
    factory { DialogColorPickerService(get()) }
    factory { DialogSelectWorkspaceService(get()) }
    single<DialogRegistry> { DialogRegistry() } bind IDialogRegister::class
}