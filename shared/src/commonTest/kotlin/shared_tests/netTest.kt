package shared_tests

import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.di.metro.CedarJamGraph
import kotlinx.coroutines.test.runTest
import shared_tests.base.AppEnvironmentTest
import kotlin.test.Test

class netTest : AppEnvironmentTest() {

    @Test
    fun testNet() = runTest {
        val syncNetRepository = CedarJamGraph.instance.cedarJamDependencies.remoteSyncRepository
        val result = syncNetRepository.workspaceFiles(
            userName = "bulat",
            workspaceName = "abc"
        )
        result.shouldBeSuccess()
    }

    @Test
    fun adad() = runTest {
        val workspaceEnv = createWorkspaceEnv()

        val textBytes = "hehehehe".encodeToByteArray()
        workspaceEnv.createFileNode(
            "",
            fileName = FileName(name = "ase", extension = "md"),
            isAbsoluteNew = true,
            textBytes
        )
//        val result = workspaceEnv.uploadSync(,)
//        val resultDownload = workspaceEnv.downloadSync()
    }

    @Test
    fun adaddd() = runTest {
        val workspaceEnv = createWorkspaceEnv()
//
//        val textBytes = "hehehehe".encodeToByteArray()
//        workspaceEnv.createFileNode(
//            null,
//            fileName = FileName(name = "ase", extension = "md"),
//            isAbsoluteNew = true,
//            textBytes
//        )
//        val result = workspaceEnv.getSyncChanges()
//        val resultDownload = workspaceEnv.downloadSync()
    }
}