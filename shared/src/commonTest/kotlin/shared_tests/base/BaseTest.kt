package shared_tests.base

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.projectDir
import kotlinx.coroutines.flow.first

abstract class BaseTest : MultiplatformTest() {

    suspend fun IWorkspaceEnvironment.logFiles() {
        val files =
            (this.getFileNodesFlow().first().getOrNull() ?: listOf()).getAllFilesByExtension(null)
        Logger.d("files: ${files.joinToString("\n") { d -> d.getFullPath() }}")
    }

    private val testWorkspaceName = "hehe"

    val buildPath: String
        get() = pathWrapper(
            FileKit.projectDir.absolutePath().dropLast(1),
            "build",
            "_1_unit_tests"
        ).pathString

    fun WorkspacePresentation.getTestWorkspace(): Workspace {
        return this.getTestWorkspace()
    }

    fun getTestFullPath(): String {
        return pathWrapper(
            buildPath,
            "workspaces",
            testWorkspaceName
        ).pathString
    }

    fun getTestWorkspace(): Workspace {
        return Workspace(
            name = testWorkspaceName,
            platformPath = getTestFullPath(),
            serverName = testWorkspaceName
        )
    }

    fun getWorkspacePresentation(): WorkspacePresentation {
        return WorkspacePresentation(
            name = getTestWorkspace().name,
            fullpath = getTestFullPath(),
            serverName = getTestWorkspace().serverName
        )
    }


}