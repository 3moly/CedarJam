package com.moly3.cedarjam.core.ui.uikit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.volumedBorderStroke
import kotlinx.collections.immutable.ImmutableList

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
            Language("简体中文", "zh"),
            Language("Español", "es"),
            Language("हिन्दी", "hi"),
            Language("Français", "fr"),
            Language("العربية", "ar"),
            Language("Português", "pt"),
            Language("বাংলা", "bn"),
            Language("Русский", "ru"),
            Language("Deutsch", "de"),
            Language("日本語", "ja"),
            Language("한국어", "ko"),
            Language("Türkçe", "tr"),
            Language("Italiano", "it"),
            Language("ไทย", "th"),
            Language("Polski", "pl"),
            Language("Українська", "uk"),
            Language("Tiếng Việt", "vi"),
            Language("Bahasa Indonesia", "id"),
            Language("Nederlands", "nl")
        )
    }
    val selectedName = remember(selectedCode) {
        options.firstOrNull { b -> b.code == selectedCode }?.name ?: selectedCode
    }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Row(
            modifier = Modifier
                .border(volumedBorderStroke, RoundedCornerShape(16.dp))
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .padding(vertical = 2.dp)
                .padding(start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CJText(modifier = Modifier.widthIn(min = 80.dp), text = selectedName)
            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = LocalAppTheme.current.colors.backgroundSecondary,
            border = volumedBorderStroke
        ) {
            for ((index, language) in options.withIndex()) {
                DropdownMenuItem(
                    text = { CJText(text = language.name) },
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