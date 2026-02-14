package com.moly3.cedarjam.core.domain.usecase

import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.flow.Flow

interface ISyncUseCase {
    suspend fun clearSending()
    fun sendingBranchFlow(): Flow<UIState<SyncStatusChannel, String>>
    suspend fun syncronize(
        workspace: IWorkspaceEnvironment,
        isAbsoluteNewLocal: Boolean
    ): ResultWrapper<SyncStatus2, String>

    suspend fun getStatus(workspace: IWorkspaceEnvironment): ResultWrapper<GetSyncStatus, String>
}

data class GetSyncStatus(
    val toUpload: ImmutableMap<String, String>,
    val toDownload: ImmutableMap<String, String>,
)

data class SyncStatus2(
    val isLoading: Boolean
)