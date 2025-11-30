package shared_tests.db

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import shared_tests.base.AppEnvironmentTest
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TagsTest : AppEnvironmentTest() {

    private fun getDefaultCreateRequest(): CreateTagRequest {
        return CreateTagRequest(
            name = "tag_name_123",
            color = Color.Red,
            createdTime = nowInMs()
        )
    }

    @Test
    fun create_tag() = runTest {
        val workspaceEnvironment = createWorkspaceEnv()

        val createTagRequest = getDefaultCreateRequest()
        val createdTagResult = workspaceEnvironment.createTag(createTagRequest)
        createdTagResult.shouldBeSuccess()

        val tags = workspaceEnvironment.getTagsFlow().first()
        tags.shouldHaveSize(1)

        val firstTag = tags.first()
        firstTag.id shouldBe createdTagResult.value
        firstTag.name shouldBe createTagRequest.name
        firstTag.color shouldBe createTagRequest.color
        firstTag.createdTime shouldBe createTagRequest.createdTime
    }

    @Test
    fun rename_tag() = runTest {
        val workspaceEnvironment = createWorkspaceEnv()
        val createTagRequest = getDefaultCreateRequest()
        val createdTagResult = workspaceEnvironment.createTag(createTagRequest)
        createdTagResult.shouldBeSuccess()
        val renameRequest = RenameTagRequest(
            id = createdTagResult.value,
            newName = "newName",
            modifiedTime = nowInMs()
        )
        workspaceEnvironment.renameTag(renameRequest)
        val tag = workspaceEnvironment.getTagFlow(id = createdTagResult.value).first()
        tag.shouldNotBeNull()
        tag.name shouldBe renameRequest.newName
        tag.modifiedTime shouldBe renameRequest.modifiedTime
    }

    @Test
    fun update_tag() = runTest {
        val workspaceEnvironment = createWorkspaceEnv()
        val createTagRequest = getDefaultCreateRequest()
        val createdTagResult = workspaceEnvironment.createTag(createTagRequest)
        createdTagResult.shouldBeSuccess()
        val updateTagRequest = UpdateTagRequest(
            id = createdTagResult.value,
            color = Color.Black,
            modifiedTime = nowInMs()
        )
        val updatedTagResult = workspaceEnvironment.updateTag(updateTagRequest)
        updatedTagResult.shouldBeSuccess()
        val tag = workspaceEnvironment.getTagFlow(id = createdTagResult.value).first()
        tag.shouldNotBeNull()
        tag.color shouldBe updateTagRequest.color
        tag.modifiedTime shouldBe updateTagRequest.modifiedTime
    }

    @Test
    fun delete_tag() = runTest {
        val workspaceEnvironment = createWorkspaceEnv()
        val createTagRequest = getDefaultCreateRequest()
        val createdTagResult = workspaceEnvironment.createTag(createTagRequest)
        createdTagResult.shouldBeSuccess()
        var tags = workspaceEnvironment.getTagsFlow().first()
        tags.shouldHaveSize(1)
        workspaceEnvironment.deleteTag(
            id = createdTagResult.value
        )
        tags = workspaceEnvironment.getTagsFlow().first()
        tags.shouldHaveSize(0)
    }

    @Test
    fun check_is_cascade_delete_works() = runTest {
        val workspaceEnvironment = createWorkspaceEnv()
        val createTagRequest = getDefaultCreateRequest()
        val createTag2Request = getDefaultCreateRequest().copy(name = "tag2")
        val createdTagResult = workspaceEnvironment.createTag(createTagRequest)
        val createdTag2Result = workspaceEnvironment.createTag(createTag2Request)
        createdTagResult.shouldBeSuccess()
        createdTag2Result.shouldBeSuccess()
        var tags = workspaceEnvironment.getTagsFlow().first()
        tags.shouldHaveSize(2)
        val tagToTagResult = workspaceEnvironment.createTagToTag(
            CreateTagToTagRequest(
                tagId = createdTagResult.value,
                tag2Id = createdTag2Result.value,
                modifiedTime = nowInMs()
            )
        )
        tagToTagResult.shouldBeSuccess()
        var tagToTags = workspaceEnvironment.getTagToTagsFlow().first()
        tagToTags.shouldHaveSize(1)
        workspaceEnvironment.deleteTag(id = createdTagResult.value)
        tagToTags = workspaceEnvironment.getTagToTagsFlow().first()
        tagToTags.shouldHaveSize(0)

    }
}