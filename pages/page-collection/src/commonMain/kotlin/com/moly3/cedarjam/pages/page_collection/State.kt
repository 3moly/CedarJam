package com.moly3.cedarjam.pages.page_collection

import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagCollectionRowDTO
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class State(
    val workspace: WorkspacePresentation? = null,
    val collection: CollectionDTO? = null,
    val rows: List<CollectionRowDTO> = listOf(),
    val tagCollectionRows: List<TagCollectionRowDTO> = listOf(),
    val currentPage: Long = 0L,
    val maxPage: Long = 0L,
    val tags: ImmutableList<TagDTO> = persistentListOf()
)

