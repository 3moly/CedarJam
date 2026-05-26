import co.touchlab.kermit.Logger
import java.awt.Container
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class MagnifyHandler(private val onValue: (Double) -> Unit) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method?, args: Array<Any>): Any? {
        try {
            for (o in args) {
                val mag = o.javaClass
                    .getMethod("getMagnification")
                    .invoke(o)

                // mag.getClass() should always return java.lang.Double
                // production code may wish to check double this (no pun intended)
                if (mag is Double) {
                    onValue(mag)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

fun addMagnifyListener(p: Container, onMagnifyValue: (Double) -> Unit) {
    if (System.getProperty("os.name").contains("Mac")) {
        try {
            val constructors = Class.forName("com.apple.eawt.event.GestureUtilities")
                .declaredConstructors

            var gu: Any? = null

            for (constructor in constructors) {
                constructor.isAccessible = true
                gu = constructor.newInstance()
                break
            }

            val mh = Proxy.newProxyInstance(
                Class.forName("com.apple.eawt.event.MagnificationListener").getClassLoader(),
                arrayOf<Class<*>>(Class.forName("com.apple.eawt.event.MagnificationListener")),
                MagnifyHandler { value ->
                    onMagnifyValue(value)
                }
            )
            gu!!.javaClass
                .getMethod(
                    "addGestureListenerTo",
                    Class.forName("javax.swing.JComponent"),
                    Class.forName("com.apple.eawt.event.GestureListener")
                )
                .invoke(gu, p, mh)
        } catch (e: Exception) {
//                Logger.e { e.toString() }
            //e.printStackTrace()
        }
    }
}