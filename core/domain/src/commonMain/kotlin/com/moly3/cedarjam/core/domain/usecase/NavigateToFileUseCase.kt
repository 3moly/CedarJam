package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.FileManagerService

class NavigateToFileUseCase(
    private val fileManagerService: FileManagerService,
    private val filesRepository: IFilesRepository
) : INavigateToFileUseCase {
    override suspend fun invoke(navigate: NavigateToFile): ResultWrapper<Long, String> {
        return resultBlock {
            val timestamp = when (navigate) {
                is NavigateToFile.RelativePath -> {
                    fileManagerService.openFile(
                        navigate.value,
                        isReadOnly = false
                    )
                }

                is NavigateToFile.File -> fileManagerService.openFile(
                    navigate.value,
                    isReadOnly = false
                )
            }
            timestamp
        }
        //todo navigator.justNavigate(Route.File(FilePageInput(timestamp = timestamp)))
    }
}