package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageComboBox(
    modifier: Modifier = Modifier,
    selectedCode: String,
    onSelected: (Language) -> Unit = {}
) {
    val options = remember {
        listOf(
            Language("English", "en"),
            Language("Русский", "ru"),
            Language("日本語", "ja"),
        )
    }
    val selectedName = remember(selectedCode) {
        options.firstOrNull { b -> b.code == selectedCode }?.name ?: selectedCode
    }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth(),
            readOnly = true,
            value = selectedName,
            onValueChange = {},
            label = { Text("Language") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {
                        expanded = false
                        onSelected(language)
                    }
                )
            }
        }
    }
}

data class Language(
    val name: String,
    val code: String
)