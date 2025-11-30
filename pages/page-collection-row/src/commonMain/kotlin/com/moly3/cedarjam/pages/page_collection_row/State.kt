package com.moly3.cedarjam.pages.page_collection_row

import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation

data class State(
    val connectionsCount: Int = 0,
    val workspace: WorkspacePresentation? = null,
    val collection: CollectionDTO? = null,
    val collectionRow: CollectionRowDTO? = null
)