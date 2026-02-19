package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.openDirectory
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJIOSwitch
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch

@Composable
fun DialogCreateWorkspaceService(dialog: DialogCreateWorkspaceService) {
    val scope = rememberCoroutineScope()
    CJDialogGeneric(dialog = dialog) {
        var isSelectedFullPath by remember { mutableStateOf(false) }
        var nameState by remember { mutableStateOf(TextFieldValue("")) }
        var serverNameState by remember { mutableStateOf(TextFieldValue("")) }
        var fullpathState by remember { mutableStateOf(TextFieldValue("")) }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CJText("create workspace")
            CJSearchTextField(
                value = nameState,
                placeholderText = "workspace name",
                onValueChange = {
                    nameState = it
                })

//            CJSearchTextField(
//                value = serverNameState,
//                placeholderText = "workspace server name",
//                onValueChange = {
//                    serverNameState = it
//                })
            if (getPlatform() is Platform.Jvm) {
                CJIOSwitch(height = 24, isPressed = isSelectedFullPath, onClick = {
                    isSelectedFullPath = !isSelectedFullPath
                })
                if (isSelectedFullPath) {
                    Row(Modifier.fillMaxWidth()) {
                        CJSearchTextField(
                            modifier = Modifier.weight(1f),
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
                                        serverNameState = TextFieldValue(directory.name)
                                    }
                                    fullpathState = TextFieldValue(directory.toString())
                                }
                            }
                        }
                    }
                }
            }
            CJButton(text = "Create") {
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
}