package shared_tests.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import shared_tests.base.UITest
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.router.stack.active
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.di.metro.CedarJamGraph
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.pages.page_workspace.Intent
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.delay
import shared_tests.func.checkFlowListSize
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UI1Test : UITest() {

    @Test
    fun testUITest() = runUITest(beforeSetContent = {
        Logger.d("step beforeSetContent")
    }) {
        Logger.d("step 0")
        checkAndWaitCurrentPage<Root.Child.SelectWorkspace>()
        Logger.d("step 1")
        runOnUiThread {
            component!!.onNavigate(
                Route.Workspace(
                    WorkspaceInput(
                        name = "hehe",
                        serverName = "hehe"
                    )
                )
            )
        }
        Logger.d("step 2")
        waitUntil("Navigation to Workspace", 10_000L) {
            waitForIdle() // Ensure compose updates are processed
            component!!.children.active.instance is Root.Child.Workspace
        }

        Logger.d("step 3")
    }

    @Test
    fun testUI2Test() = runUITest(beforeSetContent = {
        Logger.d("step beforeSetContent")
    }) {
        Logger.d("step 0")
        checkAndWaitCurrentPage<Root.Child.SelectWorkspace>()
        Logger.d("step 1")
        component!!.onNavigate(
            Route.Workspace(
                WorkspaceInput(
                    name = "hehe",
                    serverName = "hehe"
                )
            )
        )
        Logger.d("step 2")
        checkAndWaitCurrentPage<Root.Child.Workspace>()

        Logger.d("step 3")
    }

    private suspend inline fun IWorkspaceEnvironment.isFullSynced() {
        val graph = CedarJamGraph.instance
        val syncUseCase = graph.cedarJamDependencies.syncUseCase
        val workspaceEnv = this
        val syncStatusResult = syncUseCase.getStatus(workspaceEnv)
        syncStatusResult.shouldBeSuccess()
        syncStatusResult.value.toUpload.map { d -> d.key }.shouldHaveSize(0)
        syncStatusResult.value.toDownload.map { d -> d.key }.shouldHaveSize(0)
    }

    private suspend fun IWorkspaceEnvironment.checkDbIsEmpty() {
        getAnnotationsFlow().checkFlowListSize(expectedSize = 0)
        getCollectionsFlow().checkFlowListSize(expectedSize = 0)
        getCollectionRowsFlow(null).checkFlowListSize(expectedSize = 0)
        getTagsFlow().checkFlowListSize(expectedSize = 0)
        getTagCollectionRowsFlow().checkFlowListSize(expectedSize = 0)
        getTagFilesFlow().checkFlowListSize(expectedSize = 0)
        getTagToTagsFlow().checkFlowListSize(expectedSize = 0)
    }

    private suspend inline fun IWorkspaceEnvironment.checkServerFilesSize(expectedSize: Int) {
        val serverFilesResult2 = getServerFiles()
        serverFilesResult2.shouldBeSuccess()
        val serverFilesList = serverFilesResult2.value.files
        serverFilesList.shouldHaveSize(expectedSize)
    }

    private fun IWorkspaceEnvironment.checkDbIndexes(
        expectedSyncSize: Int,
        expectedDirtySize: Int,
        expectedNewSize: Int,
        expectedDeletedSize: Int,
    ) {
        val indexes = getIndexFiles()
        indexes.filter { d -> d.serverSyncStatus == SyncStatus.SYNCED }
            .shouldHaveSize(expectedSyncSize)
        indexes.filter { d -> d.serverSyncStatus == SyncStatus.DIRTY }
            .shouldHaveSize(expectedDirtySize)
        indexes.filter { d -> d.serverSyncStatus == SyncStatus.NEW }
            .shouldHaveSize(expectedNewSize)
        indexes.filter { d -> d.serverSyncStatus == SyncStatus.DELETED }
            .shouldHaveSize(expectedDeletedSize)
    }

    @Test
    fun testUITestLogging() = runUITest(beforeSetContent = {}) { root ->
        Logger.e { "testUITestLogging -- start" }
        Logger.e { "testUITestLogging -- =" }
        Logger.e { "testUITestLogging -- finish" }
    }

    @Test
    fun testUITestAdvance() = runUITest(beforeSetContent = {}) { root ->
        val workspace = Workspace(
            name = "hehe",
            platformPath = "build/.test_workspace_hehe",
            serverName = "hehe"
        )
        val koin = CedarJamGraph.instance
        val remoteSync = koin.cedarJamDependencies. //.get<IRemoteSyncRepository>()
        val fs = koin.get<ISystemFilesManager>()
        fs.deleteNodeHeavy(workspace.platformPath)

        remoteSync.deleteWorkspace(userName = "bulat", workspace.serverName)

        val dialog = koin.get<DialogCreateWorkspaceService>()

        val instance1 = waitAndGetComponent<Root.Child.SelectWorkspace>()
        instance1.component.onIntent(com.moly3.cedarjam.pages.page_select_workspace.Intent.CreateWorkspace)
        waitUntilAtLeastOneExists(hasText("create workspace"))
        dialog.setResult(workspace)

        val instance = waitAndGetComponent<Root.Child.Workspace>(30_000L)
        instance.component.onIntent(Intent.CreateWorkspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
        workspaceEnv.deleteWorkspaceInServer().shouldBeSuccess()
        val serverFilesResult = workspaceEnv.getServerFiles()
        serverFilesResult.shouldBeSuccess()
        val emptyWorkspaceFiles = serverFilesResult.value.files
        assertEquals(0, emptyWorkspaceFiles.size)

        val tabs = instance.component.children.value.items.first().instance
        val sre = tabs.children.value.active.instance.component as TabComponent
        val syncUseCase = koin.get<ISyncUseCase>()

        val home = waitAndTabGetComponent<TabComponent.Child.Home>(sre, 5000L)
        delay(1000L)
        root.onNavigate(Route.MainGraph)
        val graph = waitAndTabGetComponent<TabComponent.Child.Graph>(sre, 5000L)

        syncUseCase.getStatus(workspaceEnv).shouldBeSuccess()

        val dbIndexes = workspaceEnv.getIndexFiles()
        assertEquals(2, dbIndexes.size)
        syncUseCase.syncronize(workspaceEnv, isAbsoluteNewLocal = false).shouldBeSuccess()

        workspaceEnv.checkServerFilesSize(expectedSize = 2)

        workspaceEnv.isFullSynced()
        workspaceEnv.checkDbIsEmpty()

        val dR = workspaceEnv.createDirectory(null, name = "pdf", isAbsoluteNew = false)
        dR.shouldBeSuccess()

        syncUseCase.syncronize(workspaceEnv, isAbsoluteNewLocal = false).shouldBeSuccess()
        workspaceEnv.isFullSynced()

        workspaceEnv.checkServerFilesSize(expectedSize = 3)

        val fBroNode = workspaceEnv.createFileNode(
            dR.value.getRelativePath(),
            fileName = FileName("bro", extension = "txt"),
            isAbsoluteNew = false
        )
        fBroNode.shouldBeSuccess()

        syncUseCase.syncronize(workspaceEnv, isAbsoluteNewLocal = false).shouldBeSuccess()
        workspaceEnv.isFullSynced()

        workspaceEnv.checkServerFilesSize(expectedSize = 4)

        workspaceEnv.checkDbIndexes(
            expectedSyncSize = 4,
            expectedNewSize = 0,
            expectedDirtySize = 0,
            expectedDeletedSize = 0
        )
        workspaceEnv.deleteNode(fBroNode.value)
        workspaceEnv.checkDbIndexes(
            expectedSyncSize = 3,
            expectedNewSize = 0,
            expectedDirtySize = 0,
            expectedDeletedSize = 1
        )
    }
}