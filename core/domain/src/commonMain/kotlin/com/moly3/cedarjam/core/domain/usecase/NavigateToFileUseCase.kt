package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.FileManagerService

class NavigateToFileUseCase(
    private val fileManagerService: com.moly3.cedarjam.core.domain.service.FileManagerService,
    private val filesRepository: com.moly3.cedarjam.core.domain.repository.IFilesRepository
) : INavigateToFileUseCase {
    override suspend fun invoke(navigate: com.moly3.cedarjam.core.domain.model.NavigateToFile): com.moly3.cedarjam.core.domain.model.ResultWrapper<Long, String> {
        return _root_ide_package_.com.moly3.cedarjam.core.domain.model.resultBlock {
            val timestamp = when (navigate) {
                is com.moly3.cedarjam.core.domain.model.NavigateToFile.AbsolutePath -> fileManagerService.openFile(
                    navigate.value,
                    isReadOnly = false
                )

                is com.moly3.cedarjam.core.domain.model.NavigateToFile.RelativePath -> {
                    val absolutePath = filesRepository.toAbsoluteAppPath(
                        pathWrapper(
                            fileManagerService.workspacePresentation.absolutePath,
                            navigate.value
                        )
                    )
                    fileManagerService.openFile(
                        absolutePath.pathString,
                        isReadOnly = false
                    )
                }

                is com.moly3.cedarjam.core.domain.model.NavigateToFile.File -> fileManagerService.openFile(
                    navigate.value,
                    isReadOnly = false
                )
            }
            timestamp
        }
        //todo navigator.justNavigate(Route.File(FilePageInput(timestamp = timestamp)))
    }
}