package com.moly3.cedarjam.ui.pages.tags

import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagToTagDTO

data class State(
    val tags: List<TagDTO> = listOf(),
    val tagLinks: List<TagLinkDTO> = listOf(),
    val tagToTags: List<TagToTagDTO> = listOf(),
)

