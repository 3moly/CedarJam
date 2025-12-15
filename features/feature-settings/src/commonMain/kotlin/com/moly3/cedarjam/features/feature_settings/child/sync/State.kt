package com.moly3.cedarjam.features.feature_settings.child.sync

import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.usecase.SyncStatus2
import com.moly3.cedarjam.core.domain.usecase.SyncStatusChannel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class State(
    val fileVersionsState: UIState<SyncStatus2, String> = UIState.Loading,
    val uploadState: UIState<SyncStatus2, String> = UIState.Loading,
    val uploadStateChannel: UIState<SyncStatusChannel, String> = UIState.Loading,
    val indexFiles: ImmutableList<IndexFileDto> = persistentListOf(),
)