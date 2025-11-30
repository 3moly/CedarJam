package com.moly3.cedarjam.pages.page_file.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.pages.page_file.Intent
import com.moly3.cedarjam.pages.page_file.State
import com.moly3.cedarjam.core.domain.model.FileType
import com.moly3.cedarjam.core.domain.model.PageNameData
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import kotlinx.coroutines.flow.Flow

internal interface FileStore : Store<Intent, State, Unit> {
    val nameStateFlow: Flow<PageNameData?>

    sealed interface Msg {
        data class SetConnectionsCount(val value: Int) : Msg
        data class SetFileRelativePath(val value: String) : Msg
        data class SetFile(val value: FileType) : Msg
        data class SetTags(val value: List<TagDTO>) : Msg
        data class SetTagLinks(val value: List<TagLinkDTO>) : Msg
    }
}
