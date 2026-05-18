package com.moly3.cedarjam.pages.page_workspace.func

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.ui.model.FileTreeItemPresentation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay

suspend fun revealFile(
    targetPath: String,
    openedDirectories: ImmutableSet<String>,
    files: ImmutableList<FileTreeItemPresentation>
): Pair<Set<String>, Int>? {
    val openedDirectories = openedDirectories.toMutableSet()
    val revealedPath = findAndRevealFile(
        targetPath = targetPath,
        files = files,
        openedDirectories = openedDirectories
    )
    //onIntent(Intent.SetOpenedDirectories(openedDirectories.toPersistentSet()))
    return if (revealedPath != null) {
        // Wait for recomposition after opening directories
        delay(50) // Small delay to allow recomposition

        val itemIndex = findIndexInVisibleList(
            key = revealedPath,
            files = files,
            openedDirectories = openedDirectories.toPersistentList()
        )
        Logger.i("index found scroll: ${itemIndex}")
        // Calculate the index and scroll
//                        val itemIndex = calculateItemIndex(targetPath, state, openedDirectories)
        if (itemIndex != null) {
            // listState.animateScrollToItem(itemIndex)
            openedDirectories to itemIndex
        } else null
    } else null
}