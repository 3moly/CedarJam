package com.moly3.cedarjam.features.feature_settings.child.sync

import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.usecase.SyncStatus
import com.moly3.cedarjam.features.feature_settings.child.sync.model.FileVersionLine
import kotlinx.collections.immutable.ImmutableList

data class State(
    val fileVersionsState: UIState<ImmutableList<FileVersionLine>, String> = UIState.Loading,
    val uploadState: UIState<SyncStatus, String> = UIState.Loading,
)