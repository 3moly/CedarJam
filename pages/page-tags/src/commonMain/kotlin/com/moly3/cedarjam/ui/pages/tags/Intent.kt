package com.moly3.cedarjam.ui.pages.tags

import com.moly3.cedarjam.core.domain.model.TagToTagId
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest

sealed interface Intent {
    data class DeleteTagToTag(val id: TagToTagId) : Intent
    data class CreateTag(val tag: CreateTagRequest) : Intent
    data class RenameTag(val tag: TagDTO) : Intent
    data object AddTagToTag : Intent
}