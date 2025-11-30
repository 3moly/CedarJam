package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogState
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Input, Result> CJDialogGeneric(
    dialog: GlobalDialog<Input, Result>,
    content: @Composable (Input) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dialogState by dialog.inputData.collectAsState()
    val chichi = remember(dialogState) {
        when (dialogState) {
            is DialogState.Hidden -> null
            is DialogState.Opened -> (dialogState as DialogState.Opened)
        }
    }
    val state = rememberSlotModalBottomSheetState(
        child = chichi,
        skipPartiallyExpanded = true
    ) {
        content(it.inputData)
    }
    if (state.isVisible.value) {
        if (dialog.isGenericDialog) {
            val appTheme = LocalAppTheme.current
            ModalBottomSheet(
                sheetState = state.sheetState,
                sheetMaxWidth = 300.dp,
                containerColor = appTheme.colors.backgroundSecondary,
                onDismissRequest = {
                    scope.launch {
                        dialog.setResult(dialog.closeValue)
                    }
                }
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