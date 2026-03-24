package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogAddCollectionRowService
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import kotlinx.coroutines.launch

@Composable
fun DialogAddCollectionRowUI(dialog: DialogAddCollectionRowService) {
    val scope = rememberCoroutineScope()
    CJDialogGeneric(dialog = dialog) {
        var name by remember { mutableStateOf(TextFieldValue("")) }
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            CJText(text = "Add item")
            CJTextField(
                value = name,
                onValueChange = {
                    name = it
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CJButton(
                    modifier = Modifier,
                    text = "Add",
                    backColor = Color.Green,
                    onClick = {
                        scope.launch {
                            dialog.setResult(
                                CollectionRowDTO(
                                    id = 0L,
                                    name = name.text,
                                    collectionId = 0L,
                                    createdTime = 0L,
                                    modifiedTime = 0L,
                                    points = 0L
                                )
                            )
                        }
                    })
                CJButton(
                    modifier = Modifier,
                    text = "Cancel",
                    backColor = Color.Green,
                    onClick = {
                        scope.launch {
                            dialog.setResult(null)
                        }
                    })
            }
        }
    }
}