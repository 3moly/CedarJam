package shared_tests.base

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import com.kdroid.androidcontextprovider.ContextProvider
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
actual abstract class MultiplatformTest actual constructor() {

    @get:Rule(order = 1)
    val addActivityToRobolectricRule = object : TestWatcher() {
        override fun starting(description: Description?) {
            super.starting(description)
            val appContext: Application = RuntimeEnvironment.getApplication()
            ContextProvider.initialize(appContext)
            Shadows.shadowOf(appContext.packageManager).addActivityIfNotPresent(
                ComponentName(
                    appContext.packageName,
                    ComponentActivity::class.java.name,
                )
            )
        }
    }

}