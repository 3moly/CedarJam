package shared_tests.ui

import androidx.compose.runtime.Composable
import com.moly3.cedarjam.core.ui.JvmWindowScope
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle

@Composable
actual fun JvmScopeCover(content: @Composable (JvmWindowScope.() -> Unit)) {
    DecoratedWindow(
        style = DecoratedWindowStyle.dark(),
        onCloseRequest = {}) {
        content()
    }
}