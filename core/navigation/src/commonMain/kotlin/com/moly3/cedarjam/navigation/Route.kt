package com.moly3.cedarjam.navigation

import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.FilePageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.core.domain.model.WorkspaceInput

sealed interface Route {
    data object Empty : Route
    data object Back : Route
    data class Workspace(val workspace: WorkspaceInput) : Route
    data object Forward : Route
    data object MainHome : Route
    data object MainGraph : Route
    data object Tags : Route
    data class Tag(val data: TagPageInput) : Route
    data class File(val data: FilePageInput) : Route
    data class Collection(val data: CollectionPageInput) : Route
    data class CollRow(val data: CollectionRowPageInput) : Route
}