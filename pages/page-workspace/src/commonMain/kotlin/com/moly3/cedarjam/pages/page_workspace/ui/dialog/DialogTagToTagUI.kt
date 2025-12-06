package com.moly3.cedarjam.pages.page_workspace.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogTagToTagService
import com.moly3.cedarjam.core.domain.dialog.model.DialogTagToTagServiceData
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.components.SelectTagList
import com.moly3.cedarjam.core.ui.uikit.CJButton
import kotlinx.coroutines.launch

@Composable
fun DialogTagToTagUI(workspaceSession: WorkspaceSession, dialog: DialogTagToTagService) {
    val scope = rememberCoroutineScope()

    CJDialogGeneric(dialog = dialog) {
        val tags = workspaceSession.workspaceEnvStateFlow.value.getTagsFlow().collectAsState(listOf()).value

        val selectedTag1 = remember { mutableStateOf<TagDTO?>(null) }
        val selectedTag2 = remember { mutableStateOf<TagDTO?>(null) }
        Box(
            Modifier.fillMaxSize().background(Color.Green.copy(alpha = 0.3f))
                .clickable(interactionSource = null, onClick = {
                    scope.launch {
                        dialog.setResult(null)
                    }
                }),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.background(Color.Green, shape = RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                if (tags != null) {
                    Row {
                        SelectTagList(
                            tags = tags,
                            currentTag = selectedTag1.value,
                            currentTag2 = selectedTag2.value,
                            onSelectTag = {
                                selectedTag1.value = it
                            }
                        )
                        SelectTagList(
                            tags = tags,
                            currentTag = selectedTag2.value,
                            currentTag2 = selectedTag1.value,
                            onSelectTag = {
                                selectedTag2.value = it
                            }
                        )
                    }
                }
                Row(
                    Modifier

                ) {
                    CJButton(text = "Create") {
                        scope.launch {
                            val tag1 = selectedTag1.value
                            val tag2 = selectedTag2.value

                            if (tag1 == null)
                                return@launch
                            if (tag2 == null)
                                return@launch

                            dialog.setResult(
                                DialogTagToTagServiceData(
                                    firstTag = tag1,
                                    secondTag = tag2
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}