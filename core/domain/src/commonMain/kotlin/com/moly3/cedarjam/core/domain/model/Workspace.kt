package com.moly3.cedarjam.core.domain.model

import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.settings.WorkspaceSettings
import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val name: String,
    val fullpath: String
)

@Serializable
data class WorkspaceInput(val name: String)

@Serializable
data class WorkspacePresentation(
    val name: String,
    val fullpath: String,
    val absolutePath: String
)

fun WorkspacePresentation.getSettingsJsonFile(): FileTreeNode.File {
    return FileTreeNode.File(
        name = FileName("workspace_settings", extension = "json"),
        parentPath = pathWrapper(this.absolutePath, hiddenDirectory).pathString
    )
}