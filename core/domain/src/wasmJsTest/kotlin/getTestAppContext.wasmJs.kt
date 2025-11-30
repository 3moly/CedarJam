package core.domain

import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext

actual fun getTestAppContext(): AndroidApplicationContext {
    return AndroidApplicationContext()
}