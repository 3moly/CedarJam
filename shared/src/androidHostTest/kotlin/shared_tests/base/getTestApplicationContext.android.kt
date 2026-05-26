package shared_tests.base

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import org.robolectric.RuntimeEnvironment

actual fun getTestApplicationContext(): AndroidApplicationContext {
    return RuntimeEnvironment.getApplication()
}