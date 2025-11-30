package core.domain

import androidx.test.core.app.ApplicationProvider
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext

actual fun getTestAppContext(): AndroidApplicationContext {
    return ApplicationProvider.getApplicationContext()
}