package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@ExperimentalMaterial3Api
class SlotModalBottomSheetState(
    val sheetContent: State<@Composable ColumnScope.() -> Unit>,
    val sheetState: SheetState,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> rememberSlotModalBottomSheetState(
    data: T,            // This is the input (e.g., Color?)
    isOpened: Boolean,   // Source of truth for "Should be visible"
    isClosing: Boolean,
    onAnimationFinished: () -> Unit,
    sheetContent: @Composable (data: T) -> Unit,
): SlotModalBottomSheetState {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val contentBuffer = remember { mutableStateOf<(@Composable ColumnScope.() -> Unit)?>(null) }
    if (isOpened || isClosing) {
        contentBuffer.value = { sheetContent(data) }
    }
    LaunchedEffect(isOpened, isClosing) {
        if (isClosing) {
            sheetState.hide()
            onAnimationFinished()
            contentBuffer.value = null
        } else if (isOpened) {
            sheetState.show()
        }
    }
    return remember(contentBuffer.value) {
        SlotModalBottomSheetState(
            sheetContent = mutableStateOf(contentBuffer.value ?: {}),
            sheetState = sheetState,
        )
    }
}