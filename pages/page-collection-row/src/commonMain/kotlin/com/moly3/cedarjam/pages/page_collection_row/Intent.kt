package com.moly3.cedarjam.pages.page_collection_row

import com.moly3.cedarjam.core.domain.model.CollectionRowDTO

sealed interface Intent {
    data object ImportPdf : Intent
    data class SetWebLink(val value: String) : Intent
    data class Update(val collRow: CollectionRowDTO) : Intent

    data class OpenNodeData(val data: Any) : Intent
    data object OpenCollection : Intent
    data class Rename(val newName: String) : Intent
    data object OpenWorkspaceSettings : Intent
}