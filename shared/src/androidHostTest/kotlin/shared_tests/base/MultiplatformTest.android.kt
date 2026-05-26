package shared_tests.base

import org.junit.runner.RunWith
import org.robolectric.annotation.Config

//@RunWith(org.robolectric.RobolectricTestRunner::class)
//@org.robolectric.annotation.Config(
//    application = android.app.Application::class,
//    sdk = [34],
////    manifest = org.robolectric.annotation.Config.NONE,
//)
@RunWith(org.robolectric.RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)

actual abstract class MultiplatformTest actual constructor()