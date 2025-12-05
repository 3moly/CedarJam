package com.moly3.cedarjam.pages.page_home.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.pages.page_home.model.LineMatch
import com.moly3.cedarjam.core.domain.func.formatEpochMillis
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.compositions.LocalTextStyle
import com.moly3.cedarjam.core.ui.uikit.CJHighlightedText
import com.moly3.cedarjam.core.ui.uikit.CJText
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HistoryItem(
    modifier: Modifier,
    time: Long,
    typeText: String,
    text: String,
    searchText: String = "",
    matches: ImmutableList<LineMatch>? = null,
    onClick: () -> Unit
) {
    val timeText = remember(time) {
        time.formatEpochMillis()
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (searchText.isNotEmpty()) {
                CJHighlightedText(
                    modifier = Modifier,
                    text = text,
                    searchText = searchText,
                    style = LocalTextStyle.current
                )
            } else {
                CJText(
                    modifier = Modifier.weight(1f),
                    text = text,
                    style = LocalTextStyle.current
                )
            }

            CJText(
                text = timeText,
                style = LocalTextStyle.current,
                modifier = Modifier.background(Color.LightGray).padding(4.dp)
            )
            CJText(
                text = typeText,
                style = LocalTextStyle.current,
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            )
        }
        if (matches != null) {
            Column(Modifier.padding(horizontal = 16.dp).padding(vertical = 8.dp)) {
                for ((index, match) in matches.withIndex()) {
                    val shape = when (index) {
                        0 -> {
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        }

                        matches.size - 1 -> {
                            RoundedCornerShape(bottomEnd = 4.dp, bottomStart = 4.dp)
                        }

                        else -> {
                            RoundedCornerShape(0.dp)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, color = Color.LightGray, shape = shape)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CJText(
                            modifier = Modifier,
                            text = match.line.toString(),
                            style =  LocalTextStyle.current
                        )
                        CJHighlightedText(
                            modifier = Modifier,
                            text = match.text,
                            searchText = searchText,
                            style =  LocalTextStyle.current
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HistoryItemPreview() {
    Box(Modifier.height(300.dp).fillMaxWidth().background(Color(0xFF1A1A1A))) {
        //HistoryItem(Modifier, 0L, text = "")
    }
}