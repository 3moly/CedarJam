package com.moly3.cedarjam.pages.page_collection_row

import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO

data class State(
    val collection: CollectionDTO? = null,
    val collectionRow: CollectionRowDTO? = null
)