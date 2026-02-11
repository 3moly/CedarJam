package com.moly3.cedarjam.features.feature_settings.child.general

import com.arkivanov.decompose.ComponentContext
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import com.moly3.cedarjam.core.domain.repository.IFilesRepository
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsGeneralComponent(
    componentContext: ComponentContext,
    private val workspaceSession: WorkspaceSession,
    private val back: () -> Unit,
    private val close: () -> Unit
) : ISettingsGeneralComponent,
    ComponentContext by componentContext, KoinComponent {

    private val coroutineScope: CoroutineScope by inject()
    private val systemFilesManager: IFilesRepository by inject()
    override val settingsState = workspaceSession.getSettingsFlow()

    private fun setSettings(newSettings: WorkspaceSettings) {
        coroutineScope.launch(io) {
            val state = workspaceSession.getSettingsFlow().value
            if (state != newSettings) {
                workspaceSession.setSettings(newSettings)
            }
        }
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Back -> back()
            Intent.Close -> close()
            Intent.UploadFont -> coroutineScope.launch(io) {
                val extensions = listOf(
                    "ttf",
                    "otf",
                    "woff",
                    "woff2"
                )
                val file =
                    FileKit.openFilePicker(type = FileKitType.File(extensions = extensions))
                if (file != null) {
                    val workspace =
                        workspaceSession.workspaceEnvStateFlow.value.getWorkspace()

                    val fileNode = FileTreeNode.File(
                        name = FileName(name = "default", extension = "otf"),
                        parentRelativePath = hiddenDirectory,
                        workspaceFullPath = workspace.absolutePath
                    )
                    val bb = file.readBytes()
                    systemFilesManager.setNodeBytes(fileNode, byteArray = bb)
                    workspaceSession.loadLocalFont()
                }
            }

            is Intent.SetDensity -> {
                val state = workspaceSession.getSettingsFlow().value
                setSettings(
                    state.copy(
                        density = intent.density,
                        fontScale = intent.fontScale
                    )
                )
            }

            is Intent.SetLanguage -> {
                val state = workspaceSession.getSettingsFlow().value
                setSettings(
                    state.copy(
                        language = intent.code
                    )
                )
            }

            is Intent.SetTheme -> {
                val state = workspaceSession.getSettingsFlow().value
                setSettings(
                    state.copy(
                        theme = state.theme.copy(
                            colorsType = intent.colorsType,
                            colors = intent.colors
                        )
                    )
                )
            }
        }
    }
}