package com.moly3.cedarjam.ui.unused

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.moly3.cedarjam.core.ui.uikit.CJText

@Composable
fun MarkdownEditor(modifier: Modifier, value: String) {
    val cursor = remember { mutableStateOf(0) }
    val fullwidth = remember { mutableStateOf(0) }
    val markA = remember(value) {
        val splitted = value.split('\n')
        val annotatedString = buildAnnotatedString {
            for (line in splitted) {
                if (line.startsWith("![[") && line.endsWith("]]")) {
                    val imgSource = line.replace("![[", "").replace("]]", "")
                    appendInlineContent("24x", imgSource)
                } else {
                    appendLine(line)
                }

            }
        }

        annotatedString
    }
    var imageHeight by remember { mutableStateOf(32) }
    val density = LocalDensity.current
    val inlineContentMap = remember(fullwidth.value, value,imageHeight) {
        mapOf(
            "24x" to InlineTextContent(
                placeholder = Placeholder(
                    width = density.run { fullwidth.value.toSp() },
                    height = imageHeight.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Top
                )
            ) {
                Image(
                    rememberAsyncImagePainter("https://png.pngtree.com/png-clipart/20230512/original/pngtree-isolated-front-view-cat-on-white-background-png-image_9158426.png"),
                    modifier = Modifier
                        .width(density.run { fullwidth.value.toDp() })
                        .height(imageHeight.dp)
                        .clickable {
                            imageHeight = 64
                            val msg = ""
                        },
                    contentDescription = null
//                    model = it,

                )
            }
        )
    }
    var textFieldValue by remember(markA) { mutableStateOf(TextFieldValue(markA)) }
    LaunchedEffect(textFieldValue) {
        //textFieldValue.
    }
    Column(modifier = modifier.onLayoutRectChanged(callback = {
        fullwidth.value = it.width
    })) {
        CJText(
            modifier = Modifier,
            text = textFieldValue.annotatedString,
//            inlineContent = inlineContentMap,
//            onValueChange = {
//                textFieldValue = it
//            },
//            onTextLayout = { layout ->
//
//            }
        )
//        BasicTextField(
//            modifier = Modifier,
//            value = textFieldValue,
//            onValueChange = {
//                textFieldValue = it
//            },
//            onTextLayout = { layout ->
//
//            })
    }
}