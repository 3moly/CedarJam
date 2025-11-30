package com.moly3.cedarjam.ui.unused

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
//@OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalLayoutApi::class)
fun LazyCharacters() {
    val characters = getAllCommonKanji()
    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (item in characters.take(500)) {
//                Surface(
//                    modifier = Modifier.width(75.dp).height(100.dp),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text(text = item.toString(), fontSize =35.sp)
//                    }
//                }
            }
        }
    }
}

fun getAllCommonKanji(): List<Char> {
    val kanjiList = mutableListOf<Char>()

    // CJK Unified Ideographs (Common Kanji)
    for (code in 0x4E00..0x9FFF) {
        kanjiList.add(code.toChar())
    }

    // CJK Extension A (Less common but still used)
    for (code in 0x3400..0x4DBF) {
        kanjiList.add(code.toChar())
    }

    return kanjiList
}