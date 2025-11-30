package com.moly3.cedarjam.pages.page_collection

import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.domain.model.CollectionViewType
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation

sealed interface Intent {
    data class RenameCollection(val newName: String) : Intent
    data class ChangeViewType(val viewType: CollectionViewType) : Intent
    data class CreateCollectionRow(val name: String) : Intent
    data object Generate : Intent
    data object PreviousPage : Intent
    data object NextPage : Intent
    data class OpenCollectionRow(val collectionId: Long, val rowId: Long) : Intent
    data class RenameCollectionRow(val oldData: CollectionRowDTO, val newName: String) : Intent
    data class AddCollectionRowTag(val oldData: CollectionRowDTO) : Intent
    data class DeleteCollectionRow(val id: Long) : Intent
    data class OpenWebLink(val value: String) : Intent
    data class OpenDocument(val value: String) : Intent
    data class SetDocumentToRow(val row: CollectionRowDTO, val fileTreeNode: FileTreeItemPresentation) : Intent
}