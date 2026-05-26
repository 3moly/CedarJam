package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.openDirectory
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJIOSwitch
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.create_new_workspace
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogCreateWorkspaceUI(dialog: DialogCreateWorkspaceService) {
    val scope = rememberCoroutineScope()
    var isSelectedFullPath by remember { mutableStateOf(false) }
    var nameState by remember { mutableStateOf(TextFieldValue("")) }
    var fullpathState by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CJText(
            text = stringResource(Res.string.create_new_workspace),
            modifier = Modifier.padding(bottom = 24.dp).align(Alignment.CenterHorizontally)
        )
        CJSearchTextField(
            modifier = Modifier.testTag("workspace_name_input").height(32.dp),
            value = nameState,
            placeholderText = "workspace name",
            onValueChange = {
                nameState = it
            })
        if (getPlatform() is Platform.Jvm) {
            CJIOSwitch(
                modifier = Modifier.testTag("fullpath_check_box"),
                height = 24,
                checked = isSelectedFullPath,
                onCheckedChange = {
                    isSelectedFullPath = !isSelectedFullPath
                })
            if (isSelectedFullPath) {
                Row(Modifier.fillMaxWidth()) {
                    CJSearchTextField(
                        modifier = Modifier.weight(1f).testTag("workspace_fullpath_input"),
                        value = fullpathState,
                        placeholderText = "workspace fullpath",
                        onValueChange = {
                            fullpathState = it
                        })
                    CJButton(text = "D") {
                        scope.launch {
                            val directory = openDirectory()
                            if (directory != null) {
                                if (nameState.text.isEmpty()) {
                                    nameState = TextFieldValue(directory.name)
                                }
                                fullpathState = TextFieldValue(directory.toString())
                            }
                        }
                    }
                }
            }
        }
        CJButton(modifier = Modifier.testTag("workspace_name_button"), text = "Create") {
            scope.launch {
                dialog.setResult(
                    Workspace(
                        name = nameState.text,
                        serverName = nameState.text,
                        platformPath =
                            if (getPlatform() is Platform.Jvm) {
                                if (isSelectedFullPath) {
                                    fullpathState.text
                                } else {
                                    pathWrapper(
                                        FileKit.filesDir.toString(),
                                        "workspaces",
                                        nameState.text
                                    ).pathString
                                }
                            } else nameState.text
                    )
                )
            }
        }
    }
}