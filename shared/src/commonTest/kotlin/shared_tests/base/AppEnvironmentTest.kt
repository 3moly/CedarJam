package shared_tests.base

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.moly3.cedarjam.core.storage.func.init
import com.moly3.cedarjam.shared.di.initApp
import com.moly3.cedarjam.shared.di.metro.CedarJamGraph
import io.github.vinceglb.filekit.FileKit
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class AppEnvironmentTest : BaseTest() {

    private fun deleteFiles(filesStorage: ISystemFilesManager, nodes: List<FileTreeNode>) {
        for (item in nodes) {
            when (item) {
                is FileTreeNode.Directory -> {
                    deleteFiles(filesStorage, item.children)
                    filesStorage.deleteNode(item.getFullPath())
                }

                is FileTreeNode.File -> {
                    filesStorage.deleteNode(item.getFullPath())
                }
            }
        }
    }




    suspend fun createWorkspaceEnv(): IWorkspaceEnvironment {
        val workspace = getTestWorkspace()
        val appEnvironment = CedarJamGraph.instance.cedarJamDependencies.appEnvironment
        val sd = CedarJamGraph.instance.cedarJamDependencies.workspaceFactory
        val env = sd.invoke(
            appEnvironment = appEnvironment,
            workspaceInput = WorkspaceInput(workspace.name, workspace.name),
            fileManagerService = FileManagerService(FileManagerService.OpenedFiles())
        )
        env.createDatabaseFiles()
        env.createIndexDatabaseFiles()
        return env
    }

    @BeforeTest
    fun before() = runTest {
        Logger.setLogWriters(CommonWriter())
        FileKit.init(getTestApplicationContext())

        initApp(getTestApplicationContext(), isTest = true)

        val sd = CedarJamGraph.instance.cedarJamDependencies.appEnvironment
        val workspace = getWorkspacePresentation()
        sd.createWorkspace(
            Workspace(
                name = workspace.name,
                platformPath = workspace.fullpath,
                serverName = workspace.name
            )
        )
        val filesStorage = createSystemFilesManager()
        try {
            val nodes = filesStorage.getNodes(getTestFullPath())
            deleteFiles(filesStorage, nodes)
        } catch (exc: Exception) {
        }
        val workspaceEnvironment: IWorkspaceEnvironment = createWorkspaceEnv()
        //workspaceEnvironment.createDatabase()

        val collections = workspaceEnvironment.getCollectionsFlow().first()
        val rows = workspaceEnvironment.getCollectionRowsFlow(collectionId = null).first()
        val tags = workspaceEnvironment.getTagsFlow().first()
        collections.shouldHaveSize(0)
        rows.shouldHaveSize(0)
        tags.shouldHaveSize(0)
    }

    @AfterTest
    fun afterTest() {
        //stopKoin()
    }

}