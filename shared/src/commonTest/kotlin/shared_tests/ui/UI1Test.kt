package shared_tests.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import shared_tests.base.UITest
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.router.stack.active
import com.moly3.cedarjam.core.domain.dialog.DialogCreateWorkspaceService
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.usecase.ISyncUseCase
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.pages.page_workspace.Intent
import kotlinx.coroutines.delay
import org.koin.mp.KoinPlatform.getKoin
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
            component!!.childStack.active.instance is Root.Child.Workspace
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

    @Test
    fun testUITestAdvance() = runUITest(beforeSetContent = {}) { root ->
        val workspace = Workspace(
            name = "hehe",
            fullpath = "build/.test_workspace_hehe",
            serverName = "hehe"
        )
        val koin = getKoin()
        val fs = koin.get<ISystemFilesManager>()
        fs.deleteNodeHeavy(workspace.fullpath)

        val dialog = koin.get<DialogCreateWorkspaceService>()

        val instance1 = waitAndGetComponent<Root.Child.SelectWorkspace>()
        instance1.component.onIntent(com.moly3.cedarjam.pages.page_select_workspace.Intent.CreateWorkspace)
        waitUntilAtLeastOneExists(hasText("create workspace"))
        dialog.setResult(workspace)
        waitUntilAtLeastOneExists(hasText(workspace.name))
        onNodeWithText(workspace.name).performClick()
        val instance = waitAndGetComponent<Root.Child.Workspace>()
        instance.component.onIntent(Intent.CreateWorkspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
        workspaceEnv.deleteWorkspace().shouldBeSuccess()
        val serverFilesResult = workspaceEnv.getServerFiles()
        serverFilesResult.shouldBeSuccess()
        val emptyWorkspaceFiles = serverFilesResult.value.files
        assertEquals(0, emptyWorkspaceFiles.size)

        val tabs = instance.component.children.value.items.first().instance
        val sre = tabs.childStack.value.active.instance.component as TabComponent

        val home = waitAndTabGetComponent<TabComponent.Child.Home>(sre, 5000L)
        delay(1000L)
        root.onNavigate(Route.MainGraph)
        val graph = waitAndTabGetComponent<TabComponent.Child.Graph>(sre, 5000L)
        val syncUseCase = koin.get<ISyncUseCase>()
        syncUseCase.getStatus(workspaceEnv).shouldBeSuccess()

        val dbIndexes = workspaceEnv.getIndexFiles()
        assertEquals(3, dbIndexes.size)
        syncUseCase.invoke(workspaceEnv).shouldBeSuccess()

        val serverFilesResult2 = workspaceEnv.getServerFiles()
        serverFilesResult2.shouldBeSuccess()
        val files = serverFilesResult2.value.files

        val msg = "" + files
    }
}