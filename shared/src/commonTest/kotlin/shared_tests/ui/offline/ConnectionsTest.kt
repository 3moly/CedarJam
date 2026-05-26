package shared_tests.ui.offline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.moly3.cedarjam.core.domain.model.CollectionId
import com.moly3.cedarjam.core.domain.func.dbRelativePath
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.getFileTreeNodeGraphId
import com.moly3.cedarjam.core.domain.model.getTagGraphId
import com.moly3.cedarjam.core.domain.model.request.CreateAnnotationRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagAnnotationRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldContainAnyKeysOf
import shared_tests.base.UITest
import shared_tests.func.checkFlowListSize
import shared_tests.func.checkFlowMapSize
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ConnectionsTest : UITest() {

    @Test
    fun nodes_1_connections_0() = runUITest(beforeSetContent = {}) {
        val workspace = getTestWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val nodes = workspaceSession.graphEco.nodesFlow.checkFlowListSize(1)
        nodes[0].id shouldBeEqual dbRelativePath.getFileTreeNodeGraphId()
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(0)
    }

    private val tag1Request = CreateTagRequest(
        name = "Japanese",
        color = Color.Red,
        createdTime = nowInMs()
    )
    private val tag2Request = CreateTagRequest(
        name = "Grammar",
        color = Color.Green,
        createdTime = nowInMs()
    )
    private val annotationRequest = CreateAnnotationRequest(
        dataPoint = 1.0,
        dataPath = "",
        description = "",
        x = 0.0f,
        y = 0.0f,
        width = 0.0f,
        height = 0.0f,
        rowId = null
    )

    private val collectionRequest = CreateCollectionRequest(
        name = "collection",
        createdTime = nowInMs()
    )

    fun makeRowRequest(name: String = "row", collectionId: CollectionId): CreateCollectionRowRequest {
        return CreateCollectionRowRequest(
            name = name,
            collectionId = collectionId,
            createdTime = nowInMs()
        )
    }

    @Test
    fun `nodes 2 connections 0 create_tag`() = runUITest {
        val workspace = getTestWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
        workspaceEnv.createTag(tag1Request).shouldBeSuccess()
        workspaceSession.graphEco.nodesFlow.checkFlowListSize(2)
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(0)
    }


    @Test
    fun `nodes 2 connections 1 tag`() = runUITest {
        val workspace = getTestWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
        workspaceEnv.createCollection(
            CreateCollectionRequest(
                name = "123",
                createdTime = nowInMs()
            )
        ).shouldBeSuccess()
        workspaceSession.graphEco.nodesFlow.checkFlowListSize(2)
    }

    @Test
    fun `tag-to-tag add-delete`() = runUITest {
        val workspace = getTestWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
        val tag1Id = workspaceEnv.createTag(tag1Request).shouldBeSuccess()
        val tag2Id = workspaceEnv.createTag(tag2Request).shouldBeSuccess()

        val tagToTagId = workspaceEnv.createTagToTag(
            CreateTagToTagRequest(
                tagId = tag1Id,
                tag2Id = tag2Id,
                createdTime = nowInMs()
            )
        ).shouldBeSuccess()

        workspaceSession.graphEco.nodesFlow.checkFlowListSize(3)

        val connections = workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(2)
        connections.shouldContainAnyKeysOf(tag1Id.getTagGraphId(), tag2Id.getTagGraphId())
        workspaceEnv.deleteTagToTag(id = tagToTagId)
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(0)
    }

    @Test
    fun `tag-annotation add-delete`() = runUITest {
        val workspace = getTestWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

        val tag1Id = workspaceEnv.createTag(tag1Request).shouldBeSuccess()
        val annotation1Id = workspaceEnv.createAnnotation(annotationRequest).shouldBeSuccess()

        workspaceEnv.getTagAnnotationsFlow().checkFlowListSize(0)
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(2)
        val tagAnnotationId = workspaceEnv.createTagAnnotation(
            request = CreateTagAnnotationRequest(
                tagId = tag1Id,
                annotationId = annotation1Id,
                createdTime = nowInMs()
            )
        ).shouldBeSuccess()

        workspaceEnv.getTagAnnotationsFlow().checkFlowListSize(1)

        workspaceSession.graphEco.nodesFlow.checkFlowListSize(3)
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(3)
        workspaceEnv.deleteTagAnnotation(id = tagAnnotationId)
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(2)
    }

    @Test
    fun `tag-row add-delete`() = runUITest {
        val workspace = getTestWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value

        val tag1Id = workspaceEnv.createTag(tag1Request).shouldBeSuccess()
        val collectionId = workspaceEnv.createCollection(collectionRequest).shouldBeSuccess()
        val rowId =
            workspaceEnv.createRow(makeRowRequest(collectionId = collectionId))
                .shouldBeSuccess()

        workspaceEnv.getTagCollectionRowsFlow().checkFlowListSize(0)

        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(2)
        val tagRowId = workspaceEnv.createTagRow(
            request = CreateTagCollectionRowRequest(
                tagId = tag1Id,
                rowId = rowId,
                createdTime = nowInMs()
            )
        ).shouldBeSuccess()

        workspaceEnv.getTagCollectionRowsFlow().checkFlowListSize(1)
        workspaceSession.graphEco.nodesFlow.checkFlowListSize(4)
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(3)
        workspaceEnv.deleteTagCollectionRow(id = tagRowId)
        workspaceSession.graphEco.nodesFlow.checkFlowListSize(4)
        workspaceSession.graphEco.connectionsFlow.checkFlowMapSize(2)
    }
}