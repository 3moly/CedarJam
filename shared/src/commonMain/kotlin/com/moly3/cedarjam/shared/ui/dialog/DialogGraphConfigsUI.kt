package com.moly3.cedarjam.shared.ui.dialog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.moly3.cedarjam.core.domain.dialog.DialogGraphConfigsService
import com.moly3.cedarjam.core.domain.model.config.GraphSaveConfig
import com.moly3.cedarjam.core.domain.model.config.GraphSaveConfigs
import com.moly3.cedarjam.core.ui.compositions.LocalAppTheme
import com.moly3.cedarjam.core.ui.uikit.CJButton
import com.moly3.cedarjam.core.ui.uikit.CJSearchTextField
import com.moly3.cedarjam.core.ui.uikit.CJText
import com.moly3.cedarjam.core.ui.uikit.NeumorphicShape
import kotlinx.coroutines.launch
import vector.Data
import vector.TrashEmpty

@Composable
fun DialogGraphConfigsUI(
    dialog: DialogGraphConfigsService,
    input: DialogGraphConfigsService.Input
) {
    val scope = rememberCoroutineScope()
    val workspaceEnv = dialog.workspaceSession.workspaceEnvStateFlow.value
    val configsState by workspaceEnv.getGraphConfigs().collectAsState(listOf())
    val newConfigNameState = remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (item in configsState) {
                ConfigUI(
                    modifier = Modifier,
                    item = item,
                    onDeleteClick = {
                        scope.launch {
                            val result = dialog.dialogDeleteService.open(Unit)
                            if (result) {
                                val configs = configsState.toMutableList()
                                workspaceEnv.insertNewGraphConfigs(config = GraphSaveConfigs(configs.filter { d -> d.name != item.name }))
                            }
                        }
                    },
                    onInsertClick = {
                        scope.launch {
                            val configs = configsState.toMutableList()
                            val mapped = configs.map { d ->
                                if (d.name == item.name) {
                                    d.copy(part = input.part)
                                } else {
                                    d
                                }
                            }
                            workspaceEnv.insertNewGraphConfigs(config = GraphSaveConfigs(mapped))
                            dialog.setResult(null)
                        }
                    },
                    onClick = {
                        scope.launch {
                            dialog.setResult(item)
                        }
                    })
            }
        }
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .border(1.dp, shape = RoundedCornerShape(8.dp), color = Color.White)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CJSearchTextField(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                isSearchIcon = false,
                placeholderText = "New config name",
                value = newConfigNameState.value,
                onValueChange = {
                    newConfigNameState.value = it
                }
            )
            CJButton(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                text = "Add config",
                onClick = {
                    scope.launch {
                        val configs = configsState.toMutableList()
                        val trimmedName = newConfigNameState.value.text.lowercase().trim()
                        val isFound = configs.firstOrNull { d ->
                            d.name.lowercase().trim() == trimmedName
                        } != null
                        if (isFound) {
                            //todo show error
                            return@launch
                        }
                        configs.add(
                            GraphSaveConfig(
                                isPinned = false,
                                name = trimmedName,
                                part = input.part
                            )
                        )
                        workspaceEnv.insertNewGraphConfigs(config = GraphSaveConfigs(configs))
                        dialog.setResult(null)
                    }
                })
        }
    }
}

@Composable
private fun ConfigUI(
    modifier: Modifier,
    item: GraphSaveConfig,
    onInsertClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    NeumorphicShape(
        modifier = modifier.height(52.dp),
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CJText(text = item.name, modifier = Modifier.weight(1f))
                NeumorphicShape(
                    modifier = Modifier.size(36.dp),
                    painter = rememberVectorPainter(Data),
                    accentColor = LocalAppTheme.current.colors.statusBar
                ) {
                    onInsertClick()
                }
                NeumorphicShape(
                    modifier = Modifier.size(36.dp),
                    painter = rememberVectorPainter(TrashEmpty),
                    accentColor = LocalAppTheme.current.colors.statusBar
                ) {
                    onDeleteClick()
                }
            }
        },
        onClick = onClick
    )
}