package shared_tests.ui

import androidx.compose.ui.test.ExperimentalTestApi
import shared_tests.base.UITest
import co.touchlab.kermit.Logger
import com.arkivanov.decompose.router.stack.active
import com.moly3.cedarjam.navigation.Root
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class UI1Test: UITest() {

    @Test
    fun testUITest() = runUITest(beforeSetContent = {
        Logger.Companion.d("step beforeSetContent")
    }) {
        Logger.Companion.d("step 0")
        checkAndWaitCurrentPage<Root.Child.Empty>()
        Logger.Companion.d("step 1")
        runOnUiThread {
            component!!.onNavigate(
                Route.Workspace(
                    WorkspaceInput(
                        name = "hehe"
                    )
                ))
        }
        Logger.Companion.d("step 2")
        waitUntil("Navigation to Workspace", 10_000L) {
            waitForIdle() // Ensure compose updates are processed
            component!!.childStack.active.instance is Root.Child.Workspace
        }
        Logger.Companion.d("step 3")
    }

    @Test
    fun testUI2Test() = runUITest(beforeSetContent = {
        Logger.Companion.d("step beforeSetContent")
    }) {
        Logger.Companion.d("step 0")
        checkAndWaitCurrentPage<Root.Child.Empty>()
        Logger.Companion.d("step 1")
        component!!.onNavigate(
            Route.Workspace(
                WorkspaceInput(
                    name = "hehe"
                )
            ))
        Logger.Companion.d("step 2")
        checkAndWaitCurrentPage<Root.Child.Workspace>()

        Logger.Companion.d("step 3")
    }
}