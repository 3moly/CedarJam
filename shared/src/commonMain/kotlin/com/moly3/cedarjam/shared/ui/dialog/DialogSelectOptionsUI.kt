package com.moly3.cedarjam.shared.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogSelectOptionsService
import com.moly3.cedarjam.core.domain.dialog.model.DialogSelectOptionsServiceInput
import com.moly3.cedarjam.core.domain.dialog.model.SelectOption
import com.moly3.cedarjam.core.ui.dialog.DialogRegistry
import com.moly3.cedarjam.core.ui.uikit.AppThemePreview
import com.moly3.cedarjam.core.ui.uikit.CJButton
import kotlinx.collections.immutable.persistentListOf

@Composable
fun DialogSelectOptionsUI(
    dialog: DialogSelectOptionsService,
    input: DialogSelectOptionsServiceInput
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
//            CJText(text = "Delete this item?", fontSize = 24.sp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (option in input.options) {
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

@Preview
@Composable
private fun DialogSelectOptionsUIPreview() {
    AppThemePreview {
        val dialog = DialogSelectOptionsService(DialogRegistry())

        val input = DialogSelectOptionsServiceInput(
            options = persistentListOf(
                SelectOption(text = "rename", {}),
                SelectOption(text = "change type", {}),
            )
        )
        dialog.openImmediate(
            input
        )

        DialogSelectOptionsUI(dialog = dialog, input = input)
    }
}