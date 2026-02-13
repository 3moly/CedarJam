package com.moly3.cedarjam.features.feature_settings.child.storage

import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.UIState
import kotlinx.collections.immutable.ImmutableList

data class State(
    val filesState: UIState<ImmutableList<FileTreeNode>, String> = UIState.Loading,
    val allFilesState: UIState<ImmutableList<FileTreeNode>, String> = UIState.Loading,
    val tagsCount: Int = 0,
    val collectionsCount: Int = 0,
    val rowsCount: Int = 0,
    val tagToTagsCount: Int = 0,
    val tagToRowsCount: Int = 0,
    val tagToFilesCount: Int = 0,
    val annotationsCount: Int = 0,
)