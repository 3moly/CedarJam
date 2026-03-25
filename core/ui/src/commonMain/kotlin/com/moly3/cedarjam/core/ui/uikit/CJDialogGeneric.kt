package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogState
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog
import com.moly3.cedarjam.core.domain.dialog.getData
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.func.rememberWindowSize
import com.moly3.cedarjam.core.ui.model.WindowSize
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Input, Result> CJDialogGeneric(
    dialog: GlobalDialog<Input, Result>,
    stackOffset: Int = 0, // Default to 0 (front)
    content: @Composable (Input) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dialogState by dialog.inputData.collectAsState()

    val offsetX = (-60 * stackOffset).dp
    val animatedOFfset by animateDpAsState(offsetX)

    val currentData = dialogState.getData()

    // Explicitly determine state
    val isOpened = dialogState is DialogState.Opened
    val isClosing = dialogState is DialogState.Closing

    val state = rememberSlotModalBottomSheetState(
        data = currentData as Input, // This can now be NULL safely
        isOpened = isOpened,         // The helper uses this for visibility
        isClosing = isClosing,
        onAnimationFinished = {
            scope.launch { dialog.confirmHidden() }
        }
    ) { input ->
        content(input)
    }

    if (dialogState !is DialogState.Hidden) {
        if (dialog.isGenericDialog) {
            val appTheme = LocalAppTheme.current
            ModalBottomSheet(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = animatedOFfset.toPx()
                    },
                sheetState = state.sheetState,
                sheetMaxWidth = 400.dp,
                containerColor = appTheme.colors.backgroundSecondary,
                properties = ModalBottomSheetProperties(),
                onDismissRequest = {
                    scope.launch {
                        // Triggers the animation via the service
                        dialog.requestClose(dialog.closeValue)
                    }
                },
                dragHandle = null
            ) {
                Column {
                    state.sheetContent.value(this)
                }
            }
        } else {
            Column {
                state.sheetContent.value(this)
            }
        }
    }
}