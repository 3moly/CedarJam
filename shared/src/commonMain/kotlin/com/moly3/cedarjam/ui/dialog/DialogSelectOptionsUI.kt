package com.moly3.cedarjam.ui.dialog

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
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectOptionsService
import com.moly3.cedarjam.core.domain.dialog.model.DialogSelectOptionsServiceInput
import com.moly3.cedarjam.core.domain.dialog.model.SelectOption
import com.moly3.cedarjam.core.ui.uikit.AppThemePreview
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun DialogSelectOptionsUI(dialog: DialogSelectOptionsService) {
    CJDialogGeneric(dialog = dialog) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
//            CJText(text = "Delete this item?", fontSize = 24.sp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (option in it.options) {
                    CJButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        text = option.text,
                        onClick = option.onClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DialogSelectOptionsUIPreview() {
    AppThemePreview {
        val dialog = DialogSelectOptionsService()

        dialog.openImmediate(
            DialogSelectOptionsServiceInput(
                options = persistentListOf(
                    SelectOption(text = "rename", {}),
                    SelectOption(text = "change type", {}),
                )
            )
        )

        DialogSelectOptionsUI(dialog = dialog)
    }
}