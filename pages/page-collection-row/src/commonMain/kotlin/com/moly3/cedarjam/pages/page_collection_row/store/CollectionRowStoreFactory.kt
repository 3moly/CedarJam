package com.moly3.cedarjam.pages.page_collection_row.store

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.moly3.cedarjam.navigation.BaseExecutor
import com.moly3.cedarjam.navigation.Navigator
import com.moly3.cedarjam.navigation.Route
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.CollectionRowPageInput
import com.moly3.cedarjam.core.domain.model.navigation.input.TagPageInput
import com.moly3.cedarjam.pages.page_collection_row.Intent
import com.moly3.cedarjam.pages.page_collection_row.State
import com.moly3.cedarjam.core.ui.func.getPdfResult
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.model.FileName
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.NavigateToFile
import com.moly3.cedarjam.core.ui.model.PageNameData
import com.moly3.cedarjam.core.domain.model.TagDTO
import com.moly3.cedarjam.core.domain.model.getCollectionRowGraphId
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.mapToUpdateRequest
import com.moly3.cedarjam.core.domain.service.FileManagerService
import com.moly3.cedarjam.core.domain.service.WorkspaceSession
import com.moly3.cedarjam.core.domain.usecase.INavigateToFileUseCase
import com.moly3.cedarjam.core.ui.model.CJText
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType.*
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

internal class CollectionRowStoreFactory(
    private val workspaceSession: WorkspaceSession,
    private val storeFactory: StoreFactory,
    private val lifecycle: Lifecycle,
    private val pageInput: CollectionRowPageInput,
    private val openWorkspaceSettings: (Boolean) -> Unit
) : KoinComponent {
    private val fileManagerService: FileManagerService by lazy {
        workspaceSession.fileManagerService
    }
    private val coroutineScope: CoroutineScope by inject()
    private val navigator: Navigator by inject()
    private val navigateToFileUseCase: INavigateToFileUseCase by inject {
        parametersOf(fileManagerService)
    }

    fun create(): CollectionRowStore = object : CollectionRowStore,
        Store<Intent, State, Unit> by storeFactory.create(
            name = CollectionRowStore::class.simpleName,
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {
        override val nameStateFlow: StateFlow<PageNameData?>
            get() = workspaceSession.collectionRowsFlow.map {
                val row = it.firstOrNull { b -> b.id == pageInput.rowId }
                if (row == null) {
                    null
                } else {
                    PageNameData(
                        name = CJText.Raw(row.name),
                        pageType = PageNameData.PageType.CollectionRow(id = row.id),
                        modifiedTime = row.modifiedTime
                    )
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                initialValue = null
            )
    }

    private inner class ExecutorImpl :
        BaseExecutor<Intent, Unit, State, CollectionRowStore.Msg, Unit>(lifecycle) {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun onStart(scopeFromStartToStop: CoroutineScope) {
            super.onStart(scopeFromStartToStop)

            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow
                    .collectLatest {
                        dispatch(CollectionRowStore.Msg.SetWorkspace(it.getWorkspace()))
                    }
            }
            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow
                    .flatMapLatest {
                        it.getCollectionRowFlow(rowId = pageInput.rowId)
                    }.collectLatest {
                        dispatch(CollectionRowStore.Msg.SetCollectionRow(it))
                    }
            }
            scopeFromStartToStop.launch {
                workspaceSession.workspaceEnvStateFlow
                    .flatMapLatest {
                        it.getCollectionFlow(collectionId = pageInput.collectionId)
                    }.collectLatest {
                        dispatch(CollectionRowStore.Msg.SetCollection(it))
                    }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {

                Intent.ImportPdf -> {
                    scope.launch {
                        val file =
                            FileKit.openFilePicker(type = File(extension = "pdf"))
                        val row = state().collectionRow

                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        if (file != null && row != null) {
                            val workspace = workspaceEnv.getWorkspace()
                            val relativePathFile = pathWrapper(
                                hiddenDirectory,
                                "resources",
                                file.extension,
                                "${file.nameWithoutExtension}.${file.extension}"
                            )
                            val relativePath = pathWrapper(
                                hiddenDirectory,
                                "resources",
                                file.extension
                            )
                            val newFile = FileTreeNode.File(
                                name = FileName(
                                    name = file.nameWithoutExtension,
                                    extension = file.extension
                                ),
                                workspaceFullPath = workspace.fullpath,
                                parentRelativePath = relativePath.pathString
                            )
                            workspaceEnv.copyFile(
                                newFile = newFile,
                                byteArray = file.readBytes()
                            )
                            delay(1500L)
                            val relativePath2 = relativePathFile.toString()

                            Logger.e { "row book: ${newFile.getFullPath()} " }
                            val pdfResult = try {
                                getPdfResult(newFile.getFullPath())
                            } catch (exc: Exception) {
                                null
                            }
                            var copiedRow = row.copy(
                                fileRelativePath = relativePath2,
//                                    progressMax = pdfResult.numberOfPages.toDouble(),
                                modifiedTime = nowInMs()
                            )
                            if (pdfResult != null) {
                                Logger.w { "row book: success: ${pdfResult.numberOfPages}" }
                                copiedRow =
                                    copiedRow.copy(progressMax = pdfResult.numberOfPages.toDouble())
                            }
                            workspaceEnv.updateCollectionRow(
                                copiedRow.mapToUpdateRequest()
                            )
                        }
                    }
                }
                is Intent.OpenWorkspaceSettings->{
                    openWorkspaceSettings(true)
                }

                is Intent.Rename -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        workspaceEnv.renameCollectionRow(
                            request = RenameDataCollectionRowRequest(
                                rowId = pageInput.rowId,
                                newName = intent.newName,
                                modifiedTime = nowInMs()
                            )
                        )
                    }
                }

                is Intent.OpenCollection -> {
                    navigator.navigate(
                        Route.Collection(CollectionPageInput(pageInput.collectionId))
                    )
                }

                is Intent.OpenNodeData -> {
                    scope.launch {
                        val data = intent.data
                        if (data is FileTreeNode) {
                            navigateToFileUseCase.invoke(NavigateToFile.File(data))
                        } else if (data is TagDTO) {
                            navigator.navigate(
                                Route.Tag(TagPageInput(id = data.id))
                            )
                        }

                    }
                }

                is Intent.Update -> {
                    scope.launch {
                        val workspaceEnv = workspaceSession.workspaceEnvStateFlow.value
                        workspaceEnv.updateCollectionRow(
                            request = intent.collRow.copy(modifiedTime = nowInMs())
                                .mapToUpdateRequest()
                        )
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, CollectionRowStore.Msg> {
        override fun State.reduce(msg: CollectionRowStore.Msg): State {
            return when (msg) {
                is CollectionRowStore.Msg.SetCollectionRow -> copy(collectionRow = msg.value)
                is CollectionRowStore.Msg.SetCollection -> copy(collection = msg.value)
                is CollectionRowStore.Msg.SetWorkspace -> copy(workspace = msg.value)
                is CollectionRowStore.Msg.SetConnectionsCount -> copy(connectionsCount = msg.value)
            }
        }
    }
}