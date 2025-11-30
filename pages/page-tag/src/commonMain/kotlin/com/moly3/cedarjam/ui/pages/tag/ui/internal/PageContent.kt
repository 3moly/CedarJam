package com.moly3.cedarjam.ui.pages.tag.ui.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moly3.cedarjam.ui.pages.tag.Intent
import com.moly3.cedarjam.ui.pages.tag.State
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJCircularProgressIndicator
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.CJGraphPresentationUI
import com.moly3.cedarjam.core.ui.vectors.SquareHelp
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
internal fun PageContent(
    state: State,
    onIntent: (Intent) -> Unit
) {
    when (state.tagState) {
        is UIState.Error<*> -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = rememberVectorPainter(SquareHelp), contentDescription = null)
                }
            }
        }

        UIState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CJCircularProgressIndicator()
            }
        }

        is UIState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item("top_block") {
                    CJText(text = state.tagState.data.name)

                    CJButton(text = "Link tag to tag") {
                        onIntent(Intent.SetNewTag)
                    }
                    CJText(
                        modifier = Modifier.padding(top = 12.dp),
                        text = "# linked connections",
                        fontSize = 16.sp
                    )
                }

                for (file in state.connections) {
                    item(file.toString()) {
                        CJGraphPresentationUI(
                            modifier = Modifier,
                            data = file,
                            onClick = {
                                onIntent(Intent.OpenLink(file))
                            },
                            onDelete = {
                                onIntent(Intent.DeleteLink(file))
                            })
                    }
                }
            }
        }
    }
}