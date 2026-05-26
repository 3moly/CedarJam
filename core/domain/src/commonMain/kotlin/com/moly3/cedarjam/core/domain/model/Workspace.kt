package com.moly3.cedarjam.core.domain.model

import androidx.compose.runtime.Stable
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.func.getWorkspacesFolder
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.pathWrapper
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.filesDir
import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val name: String,
    val serverName: String,
    val platformPath: String
)

@Serializable
data class WorkspaceInput(val name: String, val serverName: String)

@Serializable
@Stable
data class WorkspacePresentation(
    val name: String,
    val serverName: String,
    val fullpath: String
) {
    val absolutePath: String
        get() = pathWrapper(
            when(getPlatform()){
                Platform.Android,
                Platform.Ios -> FileKit.filesDir.toString()
                Platform.Jvm,
                Platform.Wasm -> ""
            },
            getWorkspacesFolder(),
            fullpath
        ).pathString
}

fun WorkspacePresentation.getSettingsJsonFile(): FileTreeNode.File {
    return FileTreeNode.File(
        workspaceFullPath = absolutePath,
        parentRelativePath = hiddenDirectory,
        name = FileName("workspace_settings", extension = "json"),
    )
}

fun WorkspacePresentation.getGraphConfigs(): FileTreeNode.File {
    return FileTreeNode.File(
        workspaceFullPath = absolutePath,
        parentRelativePath = hiddenDirectory,
        name = FileName("workspace_settings", extension = "json"),
    )
}