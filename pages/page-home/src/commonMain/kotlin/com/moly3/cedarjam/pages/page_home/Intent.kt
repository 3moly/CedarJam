package com.moly3.cedarjam.pages.page_home

import androidx.compose.ui.text.input.TextFieldValue

sealed interface Intent {
    data object Sync : Intent
    data object OpenTags : Intent
    data class SetSearchText(val value: TextFieldValue) : Intent
    data class OpenFileNode(val fullPath: String) : Intent
    data class OpenCollection(val id: Long) : Intent
    data class OpenTag(val id: Long) : Intent
    data class OpenRow(val id: Long, val collectionId: Long) : Intent
}