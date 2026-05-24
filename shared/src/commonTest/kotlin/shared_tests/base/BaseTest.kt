package shared_tests.base

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.navigation.Root
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

    val buildPath: String
        get() = pathWrapper(
            FileKit.projectDir.absolutePath(),
            "build",
            "_1_unit_tests"
        ).pathString

    fun getWorkspace(): Workspace {
        return Workspace(
            name = "hehe",
            platformPath = pathWrapper(
                buildPath,
                "workspaces",
                "hehe"
            ).pathString,
            serverName = "hehe"
        )
    }


}