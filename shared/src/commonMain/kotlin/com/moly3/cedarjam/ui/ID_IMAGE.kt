package com.moly3.cedarjam.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Константы для типов контента
const val ID_IMAGE = "inline_image"

@Composable
fun UnifiedObsidianEditor() {
    // Состояние текста с аннотациями (стили + вставки)
    var textState by remember { 
        mutableStateOf(
            buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                    append("Заголовок CedarJam\n")
                }
                append("Это единое текстовое поле. Курсор ходит плавно везде.\n\n")
                
                // Вставка картинки как "символа" внутри текста
                appendInlineContent(ID_IMAGE, "[image]") 
                
                append("\n\nПродолжаем писать текст после картинки...")
            }
        ) 
    }

    BasicTextField(state = TextFieldState())

    // Карта для отрисовки сложных объектов внутри текста
    val inlineContent = mapOf(
        ID_IMAGE to InlineTextContent(
            Placeholder(
                width = 300.sp,
                height = 150.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            // Здесь рисуем саму картинку или любой Compose UI
            Surface(
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Блок изображения (WYSIWYG)", fontSize = 12.sp)
                }
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        BasicTextField(
//            value = textState,
//            onValueChange = {
//                // В реальном приложении здесь будет логика парсинга Markdown на лету
//                textState = it
//            },
//            modifier = Modifier.fillMaxWidth(),
//            inlineContent = inlineContent,
//            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
//        )
    }
}



