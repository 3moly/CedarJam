package shared_tests.ui

import androidx.compose.runtime.Composable
import com.moly3.cedarjam.core.ui.JvmWindowScope

@Composable
actual fun JvmScopeCover(content: @Composable (JvmWindowScope.() -> Unit)) {
}