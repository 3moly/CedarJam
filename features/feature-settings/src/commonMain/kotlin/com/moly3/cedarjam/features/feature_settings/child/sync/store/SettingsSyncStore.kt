package com.moly3.cedarjam.features.feature_settings.child.sync.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.usecase.SyncStatus
import com.moly3.cedarjam.features.feature_settings.child.sync.Intent
import com.moly3.cedarjam.features.feature_settings.child.sync.State
import kotlinx.collections.immutable.PersistentMap

internal interface SettingsSyncStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetFileMetadata(val value: PersistentMap<String, Long>) : Msg

        data class SetPrepareStatus(val value: UIState<SyncStatus, String>) : Msg
        data class SetUploadState(val value: UIState<SyncStatus, String>) : Msg
    }
}
