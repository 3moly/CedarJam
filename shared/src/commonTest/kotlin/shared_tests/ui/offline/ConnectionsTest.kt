package shared_tests.ui.offline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.moly3.cedarjam.core.domain.func.dbRelativePath
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.getFileTreeNodeGraphId
import com.moly3.cedarjam.core.domain.model.isSuccess
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.model.success
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldHaveSize
import kotlinx.coroutines.flow.first
import shared_tests.base.UITest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ConnectionsTest : UITest() {

    @Test
    fun nodes_1_connections_0() = runUITest(beforeSetContent = {}) {
        val workspace = getWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val nodes = workspaceSession.graphEco.nodesFlow.first()
        nodes.shouldHaveSize(1)
        nodes[0].id shouldBeEqual dbRelativePath.getFileTreeNodeGraphId()
        val connections = workspaceSession.graphEco.connectionsFlow.first()
        connections.shouldHaveSize(0)
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

    @Test
    fun `nodes 2 connections 0 create_tag`() = runUITest(beforeSetContent = {}) {
        val workspace = getWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
        workspaceEnv.createTag(tag1Request).shouldBeSuccess()
        val nodes = workspaceSession.graphEco.nodesFlow.first()
        nodes.shouldHaveSize(2)
        val connections = workspaceSession.graphEco.connectionsFlow.first()
        connections.shouldHaveSize(0)
    }

    @Test
    fun `nodes 2 connections 1`() = runUITest(beforeSetContent = {}) {
        val workspace = getWorkspace()
        val instance = createWorkspace(workspace)
        val workspaceSession = instance.component.workspaceSession
        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
        val tag1Id = workspaceEnv.createTag(tag1Request).success()
        val tag2Id = workspaceEnv.createTag(tag2Request).success()

        workspaceEnv.createTagToTag(
            CreateTagToTagRequest(
                tagId = tag1Id,
                tag2Id = tag2Id,
                createdTime = nowInMs()
            )
        ).shouldBeSuccess()

        val nodes = workspaceSession.graphEco.nodesFlow.first()
        nodes.shouldHaveSize(3)

        val connections = workspaceSession.graphEco.connectionsFlow.first()
        connections.shouldHaveSize(2)
    }
}