package com.moly3.cedarjam.ui.pages.tags.store

import com.arkivanov.mvikotlin.core.store.Store
import com.moly3.cedarjam.ui.pages.tags.Intent
import com.moly3.cedarjam.ui.pages.tags.State
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagToTagDTO

internal interface TagsStore : Store<Intent, State, Unit> {

    sealed interface Msg {
        data class SetTags(val value: List<TagDTO>) : Msg
        data class SetTagLinks(val value: List<TagLinkDTO>) : Msg
        data class SetTagToTags(val value: List<TagToTagDTO>) : Msg
    }
}
