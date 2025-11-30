package com.moly3.cedarjam.pages.page_workspace.model

import androidx.compose.ui.geometry.Offset
import kotlinx.collections.immutable.ImmutableList

data class ContextMenuData(
    val targetKey: String,
    val cursorPosition: Offset,
    val menuButtons: ImmutableList<ContextMenuButton>
)

data class ContextMenuButton(
    val title: String,
    val onClick: () -> Unit
)