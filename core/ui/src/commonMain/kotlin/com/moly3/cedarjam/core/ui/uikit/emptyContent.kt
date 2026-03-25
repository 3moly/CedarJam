package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch

private val emptyContent: @Composable ColumnScope.() -> Unit = {}

@ExperimentalMaterial3Api
class SlotModalBottomSheetState(
    val sheetContent: State<@Composable ColumnScope.() -> Unit>,
    val isVisible: State<Boolean>,
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

    // This "buffer" keeps the UI alive during the closing animation
    val contentBuffer = remember { mutableStateOf<(@Composable ColumnScope.() -> Unit)?>(null) }

    // Update the buffer whenever we are in "Opened" state
    if (isOpened || isClosing) {
        contentBuffer.value = { sheetContent(data) }
    }

    // Handle the Animation Flow
    LaunchedEffect(isOpened, isClosing) {
        if (isClosing) {
            // Start the physical slide-down animation
            sheetState.hide()
            // Once finished, tell the Domain to switch to DialogState.Hidden
            onAnimationFinished()
            // Clear the buffer after it's off-screen
            contentBuffer.value = null
        } else if (isOpened) {
            sheetState.show()
        }
    }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//
//    // We need to keep a reference to the "last data" so the sheet
//    // doesn't go blank during the slide-down animation.
//    val lastData = remember { mutableStateOf<T?>(null) }
//
//    // Update the "last known good data" only when the dialog is actually open
//    if (isOpened) {
//        lastData.value = data
//    }
//
//    val childContent = remember { mutableStateOf(emptyContent) }
//
//    // Visibility Logic: Only hide if NOT opened OR if explicitly closing
//    LaunchedEffect(isOpened, isClosing) {
//        if (!isOpened || isClosing) {
//            sheetState.hide()
//            onAnimationFinished()
//        }
//    }
//
//    // Content Logic: Use the current data if open, or lastData if closing
//    DisposableEffect(isOpened, data, lastData.value) {
//        if (isOpened || isClosing) {
//            val displayData = if (isOpened) data else lastData.value
//            childContent.value = { sheetContent(displayData as T) }
//        }
//        onDispose {}
//    }

    return remember(contentBuffer.value) {
        SlotModalBottomSheetState(
            sheetContent = mutableStateOf(contentBuffer.value ?: {}),
            sheetState = sheetState,
            isVisible = mutableStateOf(isOpened || isClosing)
        )
    }
}

//@ExperimentalMaterial3Api
//@Composable
//fun <T : Any> rememberSlotModalBottomSheetState(
//    child: T?,
//    isClosing: Boolean,
//    onAnimationFinished: () -> Unit,
//    confirmValueChange: (SheetValue) -> Boolean = { true },
//    skipPartiallyExpanded: Boolean = true,
//    sheetContent: @Composable (child: T) -> Unit,
//): SlotModalBottomSheetState {
//    val sheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = skipPartiallyExpanded,
//        confirmValueChange = confirmValueChange
//    )
//
//    // 1. Keep a stable reference to the last known non-null data
//    val lastValidData = remember { mutableStateOf<T?>(null) }
//
//    // Update it whenever a new 'Opened' state provides data
//    if (child != null) {
//        lastValidData.value = child
//    }
//
//    val isVisible = remember { mutableStateOf(child != null || isClosing) }
//    val childContent = remember { mutableStateOf(emptyContent) }
//
//    // 2. Handle the visibility and animation lifecycle
//    LaunchedEffect(child == null, isClosing) {
//        if (child == null || isClosing) {
//            // Only hide if we aren't already hidden
//            if (sheetState.isVisible) {
//                sheetState.hide()
//            }
//            isVisible.value = false
//            onAnimationFinished()
//            lastValidData.value = null // Cleanup
//        } else {
//            isVisible.value = true
//        }
//    }
//
//    // 3. Update the content lambda safely
//    // This is where your NPE was: we use the backup if the primary is null
//    val displayData = child ?: lastValidData.value
//
//    // Only update the lambda if we actually have data to show
//    childContent.value = {
//        // We use displayData here because it's guaranteed non-null in this block
//        sheetContent(displayData)
//    }
//
//    return remember {
//        SlotModalBottomSheetState(
//            sheetContent = childContent,
//            sheetState = sheetState,
//            isVisible = isVisible
//        )
//    }
//}