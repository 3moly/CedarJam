package shared_tests.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeUp
import com.moly3.cedarjam.core.ui.uikit.LazyFlowRow
import com.moly3.lazyFlow.ui.LazyFlow
import com.moly3.lazyflow.core.model.FlowItemSize
import com.moly3.lazyflow.core.model.FlowOrientation
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.measureTime

@OptIn(ExperimentalTestApi::class)
class LazyFlowRowPerformanceTest {

    @Test
    fun measureScrollPerformanceInCommonCode() = runComposeUiTest {
        // 1. Setup a massive dataset
        val items = List(500) { "Item #$it" }

        // 2. Measure Initial Composition & Layout Time
        val initialRenderTime = measureTime {
            setContent {
                LazyFlow(
                    items = items,
                    orientation = FlowOrientation.Row,
                    key = { it },
                    itemSize = { FlowItemSize(widthPx = 200, heightPx = 100) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("BenchmarkFlowRow")
                ) { item ->
                    Box(modifier = Modifier.background(Color.LightGray)) {
                        Text(item)
                    }
                }
            }
            // Wait for the UI tree to fully settle before stopping the clock
            waitForIdle()
        }

        println("⏱️ Initial Render Time: ${initialRenderTime.inWholeMilliseconds} ms")

        // 3. Measure Scrolling Performance
        val node = onNodeWithTag("BenchmarkFlowRow")

        val scrollTime = measureTime {
            // Perform a massive swipe up to force new items to compose and lay out
            node.performTouchInput {
                swipeUp(startY = bottom, endY = top)
                swipeUp(startY = bottom, endY = top)
                swipeUp(startY = bottom, endY = top)
            }

            // Wait for all recompositions, animations, and measure passes to finish
            waitForIdle()
        }

        println("⏱️ Total Scroll & Recompose Time: ${scrollTime.inWholeMilliseconds} ms")

        // 4. (Optional) Fail the test if it's too slow to catch regressions in CI
        // Assuming 3 heavy swipes should take less than 200ms of raw CPU layout time
        assertTrue(
            scrollTime.inWholeMilliseconds < 200,
            "Scroll performance degraded! Took ${scrollTime.inWholeMilliseconds} ms"
        )
    }

    @Test
    fun measureScrollPerformanceInCommonCode1() = runComposeUiTest {
        val items = List(50000) { "Item #$it" }
        val initialRenderTimeV1 = measureTime {
            setContent {
                LazyFlow(
                    items = items,
                    orientation = FlowOrientation.Row,
                    key = { it },
                    itemSize = { FlowItemSize(widthPx = 200, heightPx = 100) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("BenchmarkFlowRow")
                ) { item ->
                    Box(modifier = Modifier.background(Color.LightGray)) {
                        Text(item)
                    }
                }
            }
            waitForIdle()
        }
        val node = onNodeWithTag("BenchmarkFlowRow")
        val scrollTimeV1 = measureTime {
            node.performTouchInput {
                swipeUp(startY = bottom, endY = top)
                swipeUp(startY = bottom, endY = top)
                swipeUp(startY = bottom, endY = top)
            }
            waitForIdle()
        }
        val initialRenderTimeV2 = measureTime {
            setContent {
                Column(
//                    items = items,
//                    orientation = FlowOrientation.Row,
//                    key = { it },
//                    itemSize = { FlowItemSize(widthPx = 200, heightPx = 100) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("BenchmarkFlowRow")
                ) {
                    for(item in items){
                        Box(modifier = Modifier.background(Color.LightGray)) {
                            Text(item)
                        }
                    }
                }
            }
            waitForIdle()
        }
        val node2 = onNodeWithTag("BenchmarkFlowRow")
        val scrollTimeV2 = measureTime {
            node2.performTouchInput {
                swipeUp(startY = bottom, endY = top)
                swipeUp(startY = bottom, endY = top)
                swipeUp(startY = bottom, endY = top)
            }
            waitForIdle()
        }
        println("⏱️ Initial Render Time V1: ${initialRenderTimeV1} ms")
        println("⏱️ Initial Render Time V2: ${initialRenderTimeV2} ms")
        println("⏱️ Render Time V1: ${scrollTimeV1} ms")
        println("⏱️ Render Time V2: ${scrollTimeV2} ms")
        assertTrue(
            scrollTimeV1 > scrollTimeV2,
            "v1: ${scrollTimeV1} v2: ${scrollTimeV2}"
        )
    }
}