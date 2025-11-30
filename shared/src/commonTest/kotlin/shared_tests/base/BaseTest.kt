package shared_tests.base

import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.model.FileTreeNode.Companion.getAllFilesByExtension
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import kotlinx.coroutines.flow.first

abstract class BaseTest : MultiplatformTest(){

    suspend fun IWorkspaceEnvironment.logFiles() {
        val files =
            (this.getFileNodesFlow().first().getOrNull() ?: listOf()).getAllFilesByExtension(null)
        Logger.d("files: ${files.joinToString("\n") { d -> d.getFullPath() }}")
    }
}