package shared_tests.base

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.RootComponent
import com.moly3.cedarjam.ui.MainApp
import com.moly3.cedarjam.core.domain.func.runBlocking
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.net.IRemoteSyncRepository
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.init
import com.moly3.cedarjam.core.ui.compositions.LocalUITestScope
import com.moly3.cedarjam.di.metro.CedarJamGraph
import com.moly3.cedarjam.di.metro.createCedarJamAppGraph
import com.moly3.cedarjam.di.metro.createRootComponent
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.pages.page_workspace.Intent
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import com.moly3.cedarjam.ui.Res
import com.moly3.cedarjam.ui.create_new_workspace
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.projectDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.compose.resources.getString
import kotlin.run
import kotlin.test.BeforeTest

@OptIn(ExperimentalTestApi::class)
abstract class UITest : BaseTest() {
    var component: Root? = null
    var lifecycle: LifecycleRegistry? = null

    val remoteSyncRepository: IRemoteSyncRepository
        get() =
            CedarJamGraph.instance.cedarJamDependencies.remoteSyncRepository

    val systemFileManager: ISystemFilesManager
        get() = CedarJamGraph.instance.cedarJamDependencies.systemFileManager

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun before() = runTest {
        val testAppContext = getTestApplicationContext()
        FileKit.init(testAppContext)
        initApp(testAppContext, isTest = true)
        systemFileManager.deleteNodeHeavy(buildPath)
        Logger.setLogWriters(CommonWriter())
        Logger.setMinSeverity(Severity.Verbose)
        runBlocking(Dispatchers.Main.immediate) {
            lifecycle = LifecycleRegistry()
            component = createRootComponent(
                componentContext = DefaultComponentContext(lifecycle = lifecycle!!),
                graph = createCedarJamAppGraph(),
                onDestroy = {}
            )
            lifecycle!!.resume()
        }
    }

    private fun deleteHeavy(nodePath: String) {
        val path = Path(nodePath)
        //val files = fs.list(path)
        val meta = SystemFileSystem.metadataOrNull(path) ?: return
        if (meta.isDirectory) {
            for (child in SystemFileSystem.list(path)) {
                deleteHeavy(child.toString())
            }
        }
        SystemFileSystem.delete(path, mustExist = false)
    }


    inline fun <reified CurrentPage> ComposeUiTest.checkAndWaitCurrentPage(timeoutMillis: Long = 1_000L) {
        waitUntil("", timeoutMillis) {
            currentPage<CurrentPage>()
        }
    }

    inline fun <reified CurrentPage> ComposeUiTest.waitAndGetComponent(timeoutMillis: Long = 1_000L): CurrentPage {
        waitUntil("", timeoutMillis) {
            currentPage<CurrentPage>()
        }
        return component!!.children.active.instance as CurrentPage
    }

    inline fun <reified CurrentPage> ComposeUiTest.waitAndWorkspaceGetComponent(
        component: WorkspaceComponent,
        timeoutMillis: Long = 1_000L
    ): TabsComponent {
        waitUntil("", timeoutMillis) {
            Logger.e { "first active: ${component.children.value.items.first().instance}" }
            component.children.value.items.first().instance is TabsComponent
        }
        return component.children.value.items.first().instance as TabsComponent
    }

    inline fun <reified CurrentPage> ComposeUiTest.waitAndTabGetComponent(
        component: TabComponent,
        timeoutMillis: Long = 1_000L
    ): CurrentPage {
        waitUntil("", timeoutMillis) {
            Logger.e("active component: ${component.children.active}")
            component.children.active.instance is CurrentPage
        }
        return component.children.active.instance as CurrentPage
    }


    inline fun <reified CurrentPage> currentPage(): Boolean {

        return component!!.children.active.instance is CurrentPage
    }

    @OptIn(ExperimentalTestApi::class)
    suspend fun ComposeUiTest.createWorkspace(workspace: Workspace):Root.Child.Workspace {
        val instance1 = waitAndGetComponent<Root.Child.SelectWorkspace>()
        instance1.component.onIntent(com.moly3.cedarjam.pages.page_select_workspace.Intent.CreateWorkspace)

        waitUntilAtLeastOneExists(hasText(getString(Res.string.create_new_workspace)))
        onNode(hasTestTag("fullpath_check_box")).performClick()
        onNode(hasTestTag("workspace_name_input")).performTextInput(workspace.serverName)
        waitUntilAtLeastOneExists(hasText(workspace.serverName))

        val sm = hasTestTag("workspace_fullpath_input")
        waitUntilAtLeastOneExists(sm)
        onNode(sm).performTextInput(workspace.platformPath)

        onNode(hasTestTag("workspace_name_button")).performClick()

        val instance = waitAndGetComponent<Root.Child.Workspace>(30_000L)
        instance.component.onIntent(Intent.CreateWorkspaceDatabaseFiles)
        return instance
    }

    fun runUITest(
        beforeSetContent: suspend ComposeUiTest.() -> Unit = {},
        run: suspend ComposeUiTest.(Root) -> Unit
    ) {

        runComposeUiTest() {
            beforeSetContent()
            setContent {
                CompositionLocalProvider(LocalUITestScope provides true) {
                    MainApp(component!!)
                }
            }
            waitForIdle()
            kotlinx.coroutines.runBlocking {
                run(component!!)
            }
            lifecycle?.stop()
            lifecycle?.destroy()
            while (lifecycle?.state != Lifecycle.State.DESTROYED) {
                delay(100L)
            }
        }
    }
}