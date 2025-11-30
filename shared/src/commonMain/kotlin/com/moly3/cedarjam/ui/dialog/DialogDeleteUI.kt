package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogDeleteService
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.coroutines.launch

@Composable
fun DialogDeleteUI(dialog: DialogDeleteService) {
    val scope = rememberCoroutineScope()
    CJDialogGeneric(dialog = dialog) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            CJText(text = "Delete this item?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CJButton(
                    modifier = Modifier,
                    text = "Delete",
                    backColor = Color(0xFFFE4345),
                    onClick = {
                        scope.launch {
                            dialog.setResult(true)
                        }
                    })
                CJButton(
                    modifier = Modifier,
                    text = "Cancel",
                    onClick = {
                        scope.launch {
                            dialog.setResult(false)
                        }
                    })
            }
        }
    }
}