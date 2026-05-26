package com.moly3.cedarjam.pages.page_collection_row.ui.internal

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.core.domain.model.CollectionDTO
import com.moly3.cedarjam.core.domain.model.CollectionRowDTO
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJTextField
import com.moly3.cedarjam.pages.page_collection_row.Intent

@Composable
internal fun NameField(
    collection: CollectionDTO?,
    collectionRow: CollectionRowDTO,
    scrollState: ScrollState,
    onIntent: (Intent) -> Unit
) {
    val startSize = 14f
    val textNameState = remember {
        mutableStateOf(TextFieldValue(collectionRow.name))
    }
    val isBigText =
        remember(
            scrollState.canScrollBackward,
            textNameState.value.text.length,
            collectionRow.fileRelativePath
        ) {
            if (scrollState.canScrollBackward || !collectionRow.fileRelativePath.isNullOrEmpty())
                false
            else
                textNameState.value.text.length < 6
        }
    val animatedWidthSize by animateDpAsState(if (isBigText) 100.dp else 10.dp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CJText(
                "${collection?.name}",
                color = Color.Gray,
                fontSize = startSize.sp,
                modifier = Modifier.clickable {
                    onIntent(Intent.OpenCollection)
                }
            )
            CJText("/", color = Color.Gray, fontSize = startSize.sp)
            CJText(
                "${collection?.viewType}",
                color = Color.Gray,
                fontSize = startSize.sp,
                modifier = Modifier.clickable {
                    onIntent(Intent.OpenCollection)
                }
            )
        }
        CJTextField(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .widthIn(min = animatedWidthSize),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 32.sp
            ),
            value = textNameState.value,
            imeAction = ImeAction.Done,
            onValueChange = {},
            onAnyAction = {
                if(textNameState.value.text.isEmpty()){
                    val newText = textNameState.value.text + collectionRow.name

                    textNameState.value = textNameState.value.copy(text = newText)
//                    textNameState.edit {
//                        append()
//                    }
                }else{
                    onIntent(Intent.Rename(textNameState.value.text.toString()))
                }
            }
        )
    }
}