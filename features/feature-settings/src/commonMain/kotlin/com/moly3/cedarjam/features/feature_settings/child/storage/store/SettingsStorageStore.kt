package com.moly3.cedarjam.features.feature_settings.child.storage.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.features.feature_settings.child.storage.Intent
import com.moly3.cedarjam.features.feature_settings.child.storage.State
import kotlinx.collections.immutable.ImmutableList

internal interface SettingsStorageStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetFilesState(val value: UIState<ImmutableList<FileTreeNode>, String>) : Msg
        data class SetAllFilesState(val value: UIState<ImmutableList<FileTreeNode>, String>) : Msg
        data class SetTagsCount(val value: Int) : Msg
        data class SetCollectionsCount(val value: Int) : Msg
        data class SetRowsCount(val value: Int) : Msg
        data class SetTagToTagsCount(val value: Int) : Msg
        data class SetAnnotationsCount(val value: Int) : Msg
        data class SetTagToFilesCount(val value: Int) : Msg
        data class SetTagToRowsCount(val value: Int) : Msg
    }
}
