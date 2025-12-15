package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val name: String,
    val serverName: String,
    val fullpath: String
)

@Serializable
data class WorkspaceInput(val name: String, val serverName: String)

@Serializable
@Stable
data class WorkspacePresentation(
    val name: String,
    val serverName: String,
    val fullpath: String,
    val absolutePath: String
)

fun WorkspacePresentation.getSettingsJsonFile(): FileTreeNode.File {
    return FileTreeNode.File(
        name = FileName("workspace_settings", extension = "json"),
        //todo adapt relativePath
        parentRelativePath = pathWrapper(this.absolutePath, hiddenDirectory).pathString,
        parentFullPath = pathWrapper(this.absolutePath, hiddenDirectory).pathString
    )
}