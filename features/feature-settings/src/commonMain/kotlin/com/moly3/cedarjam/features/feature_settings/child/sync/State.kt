package com.moly3.cedarjam.features.feature_settings.child.sync

import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.usecase.SyncStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class State(
    val deletedFiles: PersistentMap<String, Long> = persistentMapOf(),
    val fileVersionsState: UIState<SyncStatus, String> = UIState.Loading,
    val uploadState: UIState<SyncStatus, String> = UIState.Loading,
    val indexFiles: ImmutableList<IndexFileDto> = persistentListOf()
)