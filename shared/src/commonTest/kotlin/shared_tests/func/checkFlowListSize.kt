package shared_tests.func

import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first

suspend fun <T> Flow<List<T>>.checkFlowListSize(expectedSize: Int) {
    val list = first()
    list.shouldHaveSize(expectedSize)
}