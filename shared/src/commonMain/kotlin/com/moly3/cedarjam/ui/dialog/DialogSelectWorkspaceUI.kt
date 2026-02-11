package com.moly3.cedarjam.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.dialog.DialogSelectWorkspaceService
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.ui.uikit.CJDialogGeneric
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.UIStateContentNoBox
import kotlinx.coroutines.launch

@Composable
fun DialogSelectWorkspaceUI(
    appEnvironment: IAppEnvironment,
    dialogCreate: DialogCreateWorkspaceService,
    dialog: DialogSelectWorkspaceService
) {
//    val scope = rememberCoroutineScope()
//    CJDialogGeneric(dialog = dialog) {
//        val workspacesState by appEnvironment.getWorkspacesFlow().collectAsState()
//        Column(
//            modifier = Modifier,
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            Column(
//                modifier = Modifier,
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                UIStateContentNoBox(state = workspacesState) { workspaces ->
//                    for (workspace in workspaces) {
//                        Row(
//                            modifier = Modifier,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            CJText(text = workspace.name, modifier = Modifier.weight(1f))
//
//                            if (workspace.fullpath != it.activeWorkspace?.fullpath) {
//                                CJButton(
//                                    modifier = Modifier,
//                                    text = "select",
//                                    onClick = {
//                                        scope.launch {
//                                            dialog.setResult(workspace)
//                                        }
//                                    })
//                            }
//                            CJButton(
//                                modifier = Modifier,
//                                text = "delete",
//                                backColor = Color(0xFFFE4345),
//                                onClick = {
//                                    scope.launch {
//                                        appEnvironment.deleteWorkspace(workspace)
//                                    }
//                                })
//                        }
//                    }
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        CJButton(
//                            modifier = Modifier,
//                            text = "Add new",
//                            backColor = Color(0xFFFE4345),
//                            onClick = {
//                                scope.launch {
//                                    val result = dialogCreate.open(Unit)
//                                    if (result != null) {
//                                        appEnvironment.createWorkspace(workspace = result)
//                                    }
//                                }
//                            })
//                    }
//                }
//
//            }
//        }
//    }
}