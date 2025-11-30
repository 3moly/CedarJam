package com.moly3.cedarjam.ui.pages.tags.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.ui.pages.tags.Intent
import com.moly3.cedarjam.ui.pages.tags.State
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDTO
import com.moly3.cedarjam.core.domain.model.TagLinkDtoData
import com.moly3.cedarjam.core.domain.model.TagToTagDTO
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJDataTable
import com.moly3.cedarjam.core.ui.uikit.Header
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
internal fun PageContent(
    state: State,
    onIntent: (Intent) -> Unit
) {
    val tagsHeaders: List<Header<TagDTO>> =
        remember {
            listOf(
                Header(
                    headerName = "tag",
                    contentStr = {
                        it.name
                    }
                ),
                Header(
                    headerName = "data",
                    content = {
                        Box(Modifier.size(20.dp).background(it.color))
                    }
                ),
                Header(
                    headerName = "rename",
                    content = {
                        CJButton(
                            text = "rename"
                        ) {
                            onIntent(Intent.RenameTag(it))
                        }
                    }
                )
            )
        }
    val tagLinksHeaders: List<Header<TagLinkDTO>> =
        remember {
            listOf(
                Header(
                    headerName = "id",
                    rowWeight = 0.2f,
                    contentStr = {
                        it.id.toString()
                    }
                ),
                Header(
                    headerName = "tag",
                    rowWeight = 0.8f,
                    contentStr = {
                        it.tagId.toString()
                    }
                ),
                Header(
                    headerName = "data",
                    rowWeight = 2f,
                    contentStr = {
                        when (it.data) {
                            is TagLinkDtoData.FileNode -> (it.data as TagLinkDtoData.FileNode).relativePath
                        }
                    }
                )
            )
        }
    val tagToTagsHeaders: List<Header<TagToTagDTO>> =
        remember {
            listOf(
                Header(
                    headerName = "id",
                    rowWeight = 0.5f,
                    contentStr = {
                        it.id.toString()
                    }
                ),
                Header(
                    headerName = "1 tag",
                    contentStr = {
                        it.firstTagId.toString()
                    }
                ),
                Header(
                    headerName = "2 tag",
                    contentStr = {
                        it.secondTagId.toString()
                    }
                ),
                Header(
                    headerName = "",
                    content = {
                        CJButton(text = "delete") {
                            onIntent(Intent.DeleteTagToTag(it.id))
                        }
                    }
                )
            )
        }

    var textState by remember { mutableStateOf<TextFieldValue>(TextFieldValue()) }
    Row(modifier = Modifier.fillMaxSize()) {
        CJDataTable(
            modifier = Modifier.fillMaxHeight().weight(2f),
            headers = tagsHeaders,
            data = state.tags,
//            belowContent = {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    TTextField(
//                        modifier = Modifier.weight(1f),
//                        value = textState,
//                        onValueChanged = {
//                            textState = it
//                        }
//                    )
//                    BButton(
//                        text = "create"
//                    ) {
//                        onIntent(
//                            Intent.CreateTag(
//                                CreateTagRequest(
//                                    name = textState.text,
//                                    color = Color.Black,
//                                    createdTime = nowInMs()
//                                )
//                            )
//                        )
//                        textState = TextFieldValue("")
//                    }
//                }
//            }
        )
        CJDataTable(
            modifier = Modifier.fillMaxHeight().weight(3f),
            headers = tagLinksHeaders,
            data = state.tagLinks
        )
        CJDataTable(
            modifier = Modifier.fillMaxHeight().weight(3f),
            headers = tagToTagsHeaders,
            data = state.tagToTags,
//            belowContent = {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    BButton(
//                        text = "Add"
//                    ) {
//                        onIntent(
//                            Intent.AddTagToTag
//                        )
//                    }
//                }
//            }
        )
    }
}