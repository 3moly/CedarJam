package shared_tests

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.moly3.cedarjam.core.data.FilesRepository
import com.moly3.cedarjam.core.domain.model.TagId
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.Workspace
import com.moly3.cedarjam.core.domain.model.fold
import com.moly3.cedarjam.core.domain.model.getValueOrNull
import com.moly3.cedarjam.core.domain.model.isSuccess
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.shouldBeSuccess
import com.moly3.cedarjam.core.domain.repository.IWorkspaceEnvironment
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.storage.func.createSqlStorage
import com.moly3.cedarjam.core.storage.func.createSystemFilesManager
import com.moly3.cedarjam.pages.page_tab.TabComponentImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import shared_tests.base.AppEnvironmentTest
import shared_tests.base.getTestApplicationContext
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class dbTest : AppEnvironmentTest() {

//    @Serializable
//    data class Tag( val index: Int, val data: TagPageInput)

    @Test
    fun check_hash_code() = runTest {
        val tag = TabComponentImpl.Config.Tag(
            index = 0,
            data = TagPageInput(id = TagId(1L), isOpenGraphDialog = false)
        )
        val tag2 = TabComponentImpl.Config.Tag(
            index = 1,
            data = TagPageInput(id = TagId(1L), isOpenGraphDialog = false)
        )
        assertTrue(tag.hashCode().toString(radix = 36) != tag2.hashCode().toString(radix = 36))
    }

    @Test
    fun appEnvTest() = runTest {
//        val appEnvironment: IAppEnvironment = getKoin().get()
//        val settings = appEnvironment.getAppSettingsFlow().value
//    todo    assertTrue(settings.currentWorkspaceFullPath == null)
//        appEnvironment.setAppSettings(settings.copy(currentWorkspaceFullPath = "123123"))

    }

    @Test
    fun test_update_object() = runTest {

        val workspaceEnvironment: IWorkspaceEnvironment = createWorkspaceEnv()
        val rows = workspaceEnvironment.getCollectionRowsFlow(null).first()
        assertTrue { rows.count() == 0 }
        val collectionId = workspaceEnvironment.createCollection(
            CreateCollectionRequest(
                name = "meme",
                createdTime = nowInMs()
            )
        )
        collectionId.shouldBeSuccess()
        val createRowResult = workspaceEnvironment.createRow(
            CreateCollectionRowRequest(
                name = "are",
                collectionId = collectionId.value,
                fileRelativePath = null,
                imgRelativePath = null,
                webLink = null,
                currentProgress = null,
                progressMax = null,
                isCompleted = false,
                translation = null,
                pronunciation = null,
                exampleSentence = null,
                createdTime = 0L
            )
        )
        workspaceEnvironment.updateCollectionRow(
            request = UpdateDataCollectionRowRequest(
                id = createRowResult.getValueOrNull()!!,
                currentProgress = null,
                progressMax = null,
                webLink = null,
                fileRelativePath = null,
                imgRelativePath = null,
                isCompleted = false,
                translation = null,
                exampleSentence = null,
                pronunciation = null,
                modifiedTime = 0L
            )
        )
        val rows2 = workspaceEnvironment.getCollectionRowsFlow(null).first()
        assertTrue { rows2.count() == 1 }
    }

    @Test
    fun asKet() = runTest {
        val workspaceEnvironment: IWorkspaceEnvironment = createWorkspaceEnv()
        workspaceEnvironment.deleteTag(TagId(1L))
    }

    @Test
    fun creation() = runTest {
        val workspace = getTestWorkspace()
        val filesStorage = createSystemFilesManager()
        val sql = createSqlStorage(
            systemFilesManager = filesStorage,
            applicationProvider = AppContextProvider(getTestApplicationContext()),
            workspaceDirectoryPath = workspace.platformPath
        )
        sql.init()
    }

    @Test
    fun w_env2() = runTest {
        val env = createWorkspaceEnv()
        env.createDatabase()
        env.createDatabase()
        env.createTag(CreateTagRequest(name = "tag1", color = Color.Black, createdTime = nowInMs()))
        env.createTag(CreateTagRequest(name = "tag1", color = Color.Black, createdTime = nowInMs()))
        val tags = env.getTagsFlow().first()
        assertTrue(tags.size == 1)
    }

    @Test
    fun w_env3() = runTest {
        val env = createWorkspaceEnv()
        env.createDatabase()
        env.createDatabase()
        val createResult = env.createFileNode(
            parentRelativePath = "",
            fileName = FileName(name = "text", "md"),
            isAbsoluteNew = true,
            byteArray = null
        )
        createResult.fold({
            val expectedText = "blabla"
            env.setNodeText(
                node = it,
                text = expectedText
            )
            val actualTextResult = env.getNodeText(it)
            actualTextResult.shouldBeSuccess()
            assertTrue(actualTextResult.value == expectedText)
        }, {
            assertTrue(false)
        })
    }

    @Test
    fun w_env() = runTest {
        val env = createWorkspaceEnv()
        val nodes = env.getNodes(absolutePath = null)
        env.updateTimes()
        assertTrue { nodes.size == 1 }
        var files = env.getFileNodesFlow().first().getOrNull()
        assertTrue("actual size: ${files!!.size}") { files.size == 1 }
        env.createFileNode(
            "",
            fileName = FileName(name = "unknown", extension = "md"),
            isAbsoluteNew = false
        )
        files = env.getFileNodesFlow().first().getOrNull()
        assertTrue("actual size: ${files!!.size}") { files!!.size == 1 }
        val directory = env.createDirectory(null, name = "unknown", isAbsoluteNew = false)
        assertTrue { env.getFileNodesFlow().first().getOrNull()!!.size == 1 }

        val file =
            env.createFileNode(
                directory.getValueOrNull()!!.getRelativePath(),
                fileName = FileName(name = "unknown", extension = "md"),
                isAbsoluteNew = false
            )
        assertTrue { env.getFileNodesFlow().first().getOrNull()!!.size == 1 }
        env.deleteNode(file.getValueOrNull()!!)
        assertTrue { env.getFileNodesFlow().first().getOrNull()!!.size == 1 }
        env.logFiles()
    }

    @Test
    fun rename_file() = runTest {
        val env = createWorkspaceEnv()

        val file = env.createFileNode(
            "",
            fileName = FileName(name = "unknown", extension = "md"), isAbsoluteNew = false,
        ).getValueOrNull()!!
        val files = env.getFileNodesFlow().first().getOrNull()!!
        assertTrue("step 1 ${files.size}") { files!!.size == 1 }
        val updatedFile = env.renameNode(file, file.copy(name = file.name.copy(name = "dmitry")))
//        assertTrue("step 2") { file.modifiedTime == updatedFile.right().getOrNull() .modifiedTime }
//        assertTrue("step 3") { env.getFileNodesFlow().first().getOrNull()!!.size == 1 }
    }

//    @Test
    fun same_directory_same_file_in_times() = runTest {
        val env = createWorkspaceEnv()

        val sameName = "unknown"
        val directory = env.createDirectory(null, name = sameName, isAbsoluteNew = false)
        try {
            val file =
                env.createFileNode(
                    "",
                    fileName = FileName(name = sameName, extension = ""),
                    isAbsoluteNew = false,
                )
            assertTrue { false }
        } catch (exc: Exception) {

        }
    }

    @Test
    fun file_create_dot_saved() = runTest {
        val env = createWorkspaceEnv()
        val expectedName = "unknown."
        val result =
            env.createFileNode(
                "",
                fileName = FileName(name = expectedName, extension = ""),
                isAbsoluteNew = false,
            )
        assertTrue(result.isSuccess())
        assertTrue { result.getValueOrNull()!!.name.name == expectedName }
    }

//    @Test
//    fun test_run_arrows() = runTest {
//
//        val filesStorage = createSystemFilesManager()
//        val filesRepository = FilesRepository(filesStorage)
//
//        val file = FileTreeNode.File(
//            parentRelativePath = getWorkspaceDirectory().getFullPath(),
//            name = FileName(
//                name = "mmm",
//                extension = null
//            ),
//            parentFullPath = ""
//        )
//
//        filesRepository.createNode(workspacePath = , node = file)
////        var nodes = filesRepository.getNodes()
////        assertTrue { nodes.count() == 1 }
////        filesRepository.deleteNode(node = file)
////        nodes = filesRepository.getNodes()
////        assertTrue("huh: ${nodes.count()}") { nodes.count() == 1 }
//    }

    @Test
    fun get_text() = runTest {

        val filesStorage = createSystemFilesManager()
        val filesRepository = FilesRepository(filesStorage)

        val file = FileTreeNode.File(
            workspaceFullPath = "",
            parentRelativePath = getTestFullPath(),
            name = FileName(
                name = "mmm",
                extension = null
            ),
//            parentFullPath = ""
        )

        val text = "wasd"
        filesRepository.setNodeText(file, text)
        val getText = filesRepository.getNodeText(file)
        getText.shouldBeSuccess()
        assertTrue { getText.value == text }
    }

//    @Test
    fun move_file() = runTest {
        val filesStorage = createSystemFilesManager()
        val filesRepository = FilesRepository(filesStorage)
        val file = FileTreeNode.File(
            workspaceFullPath = getTestFullPath(),
            parentRelativePath = getTestFullPath(),
            name = FileName(
                name = "mmm",
                extension = null
            ),
//            parentFullPath = ""
        )
        val file2 = FileTreeNode.File(
            workspaceFullPath = getTestFullPath(),
            parentRelativePath =  getTestFullPath(),
            name = FileName(
                name = "mmm2",
                extension = null
            ),
//            parentFullPath = ""
        )

        val text = "wasd"
        filesRepository.setNodeText(file, text)
        var isExistsFile = filesRepository.isNodeExists(file)
        assertTrue { isExistsFile }
        filesRepository.moveNode("", file, file2)
        isExistsFile = filesRepository.isNodeExists(file)
        assertTrue { !isExistsFile }
        val isExistsFile2 = filesRepository.isNodeExists(file2)
        assertTrue { isExistsFile2 }
    }

    @Test
    fun rename_collection() = runTest {
        val env = createWorkspaceEnv()
        env.createDatabase()
        val collectionName = "solo"
        assertTrue("first is empty") { env.getCollectionsFlow().first().isEmpty() }
        val collectionId = env.createCollection(
            request = CreateCollectionRequest(
                name = collectionName,
                createdTime = nowInMs()
            )
        )
        collectionId.shouldBeSuccess()
        val list2 = env.getCollectionsFlow().first()
        assertTrue("first is empty") { list2.count() == 1 }
        val createdTime = nowInMs()
        val rowId = env.createRow(
            request = CreateCollectionRowRequest(
                name = "new",
                collectionId = collectionId.value,
                createdTime = createdTime
            )
        )
        val collectionId2 = env.createCollection(
            request = CreateCollectionRequest(
                name = collectionName + "3",
                createdTime = nowInMs()
            )
        )
//        env.renameCollection(RenameDataCollectionRequest(
//            id=
//        ))
//        assertTrue("rows is empty") { env.getCollectionRowsFlow(null).first().size == 0 }
//        val rows = env.getCollectionRowsFlow(collectionId = null).first()
//        assertTrue("rows is empty") { rows.size == 1 }
    }
}