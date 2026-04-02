package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.service.FileManagerService

fun interface NavigateToFileUseCaseFactory {
    operator fun invoke(fileManagerService: FileManagerService): INavigateToFileUseCase
}
