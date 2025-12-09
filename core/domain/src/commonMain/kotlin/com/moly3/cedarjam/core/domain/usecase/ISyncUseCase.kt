package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment

interface ISyncUseCase {
    suspend fun invoke(workspace: IWorkspaceEnvironment): ResultWrapper<SyncStatus, String>
}

data class SyncStatus(
    val filesDownloaded: List<String>
)