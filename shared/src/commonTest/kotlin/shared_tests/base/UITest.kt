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
import com.moly3.cedarjam.data.func.init
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.RootComponent
import com.moly3.cedarjam.ui.MainApp
import com.moly3.cedarjam.core.domain.func.runBlocking
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools
import kotlin.test.BeforeTest

@OptIn(ExperimentalTestApi::class)
abstract class UITest : BaseTest() {
    var component: Root? = null
    var lifecycle: LifecycleRegistry? = null

    @BeforeTest
    fun before() = runTest {
        cleanupState()
        Logger.setLogWriters(CommonWriter())
        FileKit.init(getTestApplicationContext())
        initApp(getTestApplicationContext(), isTest = true)
        runBlocking(Dispatchers.Main.immediate){
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


    inline fun <reified CurrentPage> ComposeUiTest.checkAndWaitCurrentPage() {
        waitUntil("", 1_000L) {
            currentPage<CurrentPage>()
        }
    }

    inline fun <reified CurrentPage> currentPage(): Boolean {

        return component!!.childStack.active.instance is CurrentPage
    }

    fun runUITest(beforeSetContent: ComposeUiTest.() -> Unit, run: ComposeUiTest.() -> Unit) =
        runComposeUiTest {
            beforeSetContent()
            setContent { MainApp(component!!) }
            run()

            lifecycle?.stop()
            lifecycle?.destroy()
            while (lifecycle?.state != Lifecycle.State.DESTROYED) {
                delay(100L)
            }
            stopKoin()
        }
}