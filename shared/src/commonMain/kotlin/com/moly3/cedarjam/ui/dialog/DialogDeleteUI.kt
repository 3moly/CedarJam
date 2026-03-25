package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.dialog.DialogColorPickerService
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.GlobalDialog
import com.moly3.cedarjam.core.ui.uikit.AppThemePreview
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.coroutines.launch

@Composable
fun DialogDeleteUI(
    dialog: GlobalDialog<Unit, Boolean>,
    dialogColorPickerService: DialogColorPickerService
) {
    val scope = rememberCoroutineScope()
    val leftButton = remember { FocusRequester() }
    val rightButton = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        rightButton.requestFocus()
    }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CJText(text = "Delete this item?", fontSize = 24.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CJButton(
                modifier = Modifier
                    .height(32.dp)
                    .focusRequester(leftButton)
                    .focusProperties {
                        right = rightButton
                    },
                text = "Delete",
                backColor = Color(0xFFFE4345),
                onClick = {
                    scope.launch {
                        dialog.setResult(true)
                    }
                })
            CJButton(
                modifier = Modifier
                    .height(32.dp)
                    .focusRequester(rightButton)
                    .focusProperties {
                        left = leftButton
                    },
                text = "Cancel",
                onClick = {
                    scope.launch {
                        dialogColorPickerService.open(null)
                        dialog.requestClose(false)
                    }
                })
        }
    }
}

@Preview
@Composable
fun DialogDeleteUIPreview() {
    AppThemePreview {
        val dialog = DialogDeleteService()

        dialog.openImmediate(Unit)

//        DialogDeleteUI(dialog = dialog)
    }
}