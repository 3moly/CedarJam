package shared_tests.func

import org.robolectric.shadows.ShadowLooper

actual fun runUiThreadTasksIncludingDelayedTasks() {
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
}