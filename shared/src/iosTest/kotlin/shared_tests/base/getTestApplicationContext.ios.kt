package shared_tests.base

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext

actual fun getTestApplicationContext(): AndroidApplicationContext {
    return AndroidApplicationContext()
}