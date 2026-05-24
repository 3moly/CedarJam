package shared_tests.base

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.moly3.cedarjam.di.initApp
import com.moly3.cedarjam.core.domain.func.getPlatform
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.Platform
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.WorkspaceInput
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import com.moly3.cedarjam.core.domain.repository.IAppEnvironment
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.moly3.cedarjam.core.storage.func.filesDirPath
import com.moly3.cedarjam.core.storage.func.init
import com.moly3.cedarjam.di.metro.CedarJamGraph
import io.github.vinceglb.filekit.FileKit
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class AppEnvironmentTest : BaseTest() {

    fun getWorkspaceDirectory(): FileTreeNode.Directory {
        return when (getPlatform()) {
            Platform.Android,
            Platform.Ios -> {
                FileTreeNode.Directory(
                    workspaceFullPath = "",
                    parentRelativePath = FileKit.filesDirPath(),
                    name = "/test_env2",
                    children = listOf(),
                    fileSize = 0L,
//                    parentFullPath = ""
                )
            }

            Platform.Jvm -> {
                FileTreeNode.Directory(
                    workspaceFullPath = "",
                    parentRelativePath = "build/.test_workspace",
                    name = "asa",
                    children = listOf(),
                    fileSize = 0L,
//                    parentFullPath = ""
                )
            }

            Platform.Wasm -> TODO()
        }
    }

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

    private fun getWorkspacePresentation(): WorkspacePresentation {
        val fullPath = getWorkspaceDirectory().getFullPath()
        return WorkspacePresentation(
            name = "test_env2",
            fullpath = fullPath,
            serverName = "test_env2"
        )
    }


    fun createWorkspaceEnv(): IWorkspaceEnvironment {
        val workspace = getWorkspace()
        val appEnvironment = CedarJamGraph.instance.cedarJamDependencies.appEnvironment
        val sd = CedarJamGraph.instance.cedarJamDependencies.workspaceFactory

        return sd.invoke(
            appEnvironment = appEnvironment,
            workspaceInput = WorkspaceInput(workspace.name, workspace.name),
            fileManagerService = FileManagerService(FileManagerService.OpenedFiles())
        )
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
            val nodes = filesStorage.getNodes(getWorkspaceDirectory().getFullPath())
            deleteFiles(filesStorage, nodes)
        } catch (exc: Exception) {
        }
        val workspaceEnvironment: IWorkspaceEnvironment = createWorkspaceEnv()
        workspaceEnvironment.createDatabase()

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