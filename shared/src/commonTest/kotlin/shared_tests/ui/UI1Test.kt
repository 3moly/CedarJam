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
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.f_settings_general_appearance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.test.Test

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
                        name = "hehe"
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
                    name = "hehe"
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
            fullpath = "build/.test_workspace_hehe"
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
        Logger.e { "01 CreateWorkspace" }
        val tabComponent = waitAndWorkspaceGetComponent<TabsComponent.Child.Tab>(instance.component,5000L)
        Logger.e { "02 waitAndWorkspaceGetComponent<TabsComponent.Child.Tab>(instance.component)" }
        val home = waitAndTabGetComponent<TabComponent.Child.Home>(tabComponent.component)
        instance.component.onNavigate(Route.MainGraph)
        Logger.e { "03 waitAndTabGetComponent<TabComponent.Child.Home>(tabComponent.component)" }
//        val env = instance.component.workspaceSession.workspaceEnvStateFlow.value
//        env.createFileNode(null, fileName = FileName(name="ad", extension = "md"), isAbsoluteNew = true)
        val graph = waitAndTabGetComponent<TabComponent.Child.Graph>(tabComponent.component, 5000L)
        Logger.e { "04 waitAndTabGetComponent<TabComponent.Child.Graph>(tabComponent.component, 5000L)" }
        delay(1000L)
    }
}