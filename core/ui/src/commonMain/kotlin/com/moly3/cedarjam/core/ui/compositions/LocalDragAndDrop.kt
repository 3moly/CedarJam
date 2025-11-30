package com.moly3.cedarjam.core.ui.compositions

import androidx.compose.runtime.compositionLocalOf
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation

val LocalDragAndDrop = compositionLocalOf { DragAndDropState<FileTreeItemPresentation>() }