package shared_tests.db

import androidx.compose.ui.test.ExperimentalTestApi
import shared_tests.base.AppEnvironmentTest
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.getCollectionRows
import com.moly3.cedarjam.core.domain.repository.getCollections
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.mapToUpdateRequest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CollectionsTest : AppEnvironmentTest() {

    private fun getDefaultCreateRequest(): CreateCollectionRequest {
        return CreateCollectionRequest(
            name = "tag_name_123",
            createdTime = nowInMs()
        )
    }

    @Test
    fun create_collection() = runTest {
        val workspaceEnvironment = createWorkspaceEnv()

        val createRequest = getDefaultCreateRequest()
        val createResult = workspaceEnvironment.createCollection(createRequest)
        createResult.shouldBeSuccess()

        val list = workspaceEnvironment.getCollections()
        list.shouldHaveSize(1)

        val collection = list.first()
        collection.id shouldBe createResult.value
        collection.name shouldBe createRequest.name
        collection.createdTime shouldBe createRequest.createdTime
    }

    @Test
    fun update_collection() = runTest {
        val workspaceEnvironment = createWorkspaceEnv()

        val createRequest = getDefaultCreateRequest()
        val createResult = workspaceEnvironment.createCollection(createRequest)
        createResult.shouldBeSuccess()

        val createRowResult = workspaceEnvironment.createCollectionRow(
            CreateCollectionRowRequest(
                name = "row1",
                collectionId = createResult.value,
                createdTime = nowInMs()
            )
        )
        createRowResult.shouldBeSuccess()
        val row = workspaceEnvironment.getCollectionRows(collectionId = createRowResult.value).first()
        workspaceEnvironment.updateCollectionRow(row.mapToUpdateRequest())
    }

    @Test
    fun delete_collection() = runTest {
        val env = createWorkspaceEnv()
        val createRequest = getDefaultCreateRequest()
        val createResult = env.createCollection(createRequest)
        createResult.shouldBeSuccess()
        val row = env.createCollectionRow(
            CreateCollectionRowRequest(
                name = "row1",
                collectionId = createResult.value,
                createdTime = nowInMs()
            )
        )
        row.shouldBeSuccess()
        env.getCollectionRows(collectionId = null).shouldHaveSize(1)
        env.deleteCollection(id = createResult.value)
        env.getCollectionRows(collectionId = null).shouldHaveSize(0)
    }
}