package shared_tests.base

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.RootComponent
import com.moly3.cedarjam.ui.MainApp
import com.moly3.cedarjam.core.domain.func.runBlocking
import com.moly3.cedarjam.core.storage.func.init
import com.moly3.cedarjam.pages.page_tab.TabComponent
import com.moly3.cedarjam.pages.page_tabs.TabsComponent
import com.moly3.cedarjam.pages.page_workspace.WorkspaceComponent
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools
import shared_tests.ui.JvmScopeCover
import kotlin.test.BeforeTest

@OptIn(ExperimentalTestApi::class)
abstract class UITest : BaseTest() {
    var component: Root? = null
    var lifecycle: LifecycleRegistry? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun before() = runTest {
        cleanupState()
//        Dispatchers.setMain(dispatcher)
//        Logger.setLogWriters(CommonWriter())
        FileKit.init(getTestApplicationContext())
        initApp(getTestApplicationContext(), isTest = true)
        runBlocking(Dispatchers.Main.immediate) {
            lifecycle = LifecycleRegistry()
            component = RootComponent(
                parentComponentContext = DefaultComponentContext(lifecycle = lifecycle!!)
            )
            lifecycle!!.resume()
        }
    }


    private suspend fun cleanupState() {
        // Stop lifecycle first to prevent new operations
        lifecycle?.stop()
        lifecycle?.destroy()
        lifecycle = null
        component = null

        // Allow pending operations to complete
        yield()

        // Clean shutdown of Koin with proper timeout
        var attempts = 0
        val maxAttempts = 10

        while (KoinPlatformTools.defaultContext().getOrNull() != null && attempts < maxAttempts) {
            attempts++
            try {
                Logger.d("Attempting to stop Koin (attempt $attempts)")
                stopKoin()
                Logger.d("Koin stopped successfully")
                break
            } catch (exc: Exception) {
                Logger.w("Failed to stop Koin on attempt $attempts: ${exc.message}")
                if (attempts >= maxAttempts) {
                    Logger.e("Max attempts reached, forcing cleanup")
                    break
                }
                delay(100L)
            }
        }

        // Give some time for any remaining cleanup
        delay(200L)
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
        return component!!.childStack.active.instance as CurrentPage
    }

    inline fun <reified CurrentPage> ComposeUiTest.waitAndWorkspaceGetComponent(
        component: WorkspaceComponent,
        timeoutMillis: Long = 1_000L
    ): TabsComponent {
        waitUntil("", timeoutMillis) {
            Logger.e{ "first active: ${component.children.value.items.first().instance}" }
            component.children.value.items.first().instance is TabsComponent
        }
        return component.children.value.items.first().instance as TabsComponent
    }

    inline fun <reified CurrentPage> ComposeUiTest.waitAndTabGetComponent(
        component: TabComponent,
        timeoutMillis: Long = 1_000L
    ): CurrentPage {
        waitUntil("", timeoutMillis) {
            Logger.e("active component: ${component.childStack.active}")
            component.childStack.active.instance is CurrentPage
        }
        return component.childStack.active.instance as CurrentPage
    }


    inline fun <reified CurrentPage> currentPage(): Boolean {

        return component!!.childStack.active.instance is CurrentPage
    }

    fun runUITest(
        beforeSetContent: suspend ComposeUiTest.() -> Unit,
        run: suspend ComposeUiTest.(Root) -> Unit
    ) =
        runComposeUiTest {
            beforeSetContent()
            setContent {
                JvmScopeCover {
                    MainApp(component!!)
                }
            }
            kotlinx.coroutines.runBlocking {
                run(component!!)
            }
//            io.kotest.engine.runBlocking {
//
//            }
            lifecycle?.stop()
            lifecycle?.destroy()
            while (lifecycle?.state != Lifecycle.State.DESTROYED) {
                delay(100L)
            }
            stopKoin()
        }
}