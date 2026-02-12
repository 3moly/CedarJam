package com.moly3.cedarjam.core.domain.service

import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.WorkspacePresentation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlin.collections.get
import kotlin.collections.iterator
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
class FileManagerService(
    val workspacePresentation: WorkspacePresentation,
    startedState: OpenedFiles
) {

    @Serializable
    data class FileNodeSeconds(
        val fileNodeRelativePath: String = "",
        val refreshToken: Long? = null
    )

    @Serializable
    data class OpenedFiles(
        val states: Map<Long, FileNodeSeconds> = mapOf()
    )

    private val _openedFilesState = MutableStateFlow<OpenedFiles>(startedState)
    private val _closeDeletedFile = MutableSharedFlow<Long?>()
    val fileNodeState = _openedFilesState.asStateFlow()
    val closeDeletedFile: SharedFlow<Long?> = _closeDeletedFile

    suspend fun deleteFile(timestamp: Long) {
        _closeDeletedFile.emit(timestamp)
    }


    suspend fun openFile(fileNode: FileTreeNode, isReadOnly: Boolean): Long {
        return openFile(fileNode.getRelativePath(), isReadOnly)
    }

    suspend fun openFile(relativePath: String, isReadOnly: Boolean): Long {
        val alreadyAdded = _openedFilesState.value
            .states
            .asSequence()
            .firstOrNull { d -> d.value.fileNodeRelativePath == relativePath }
        val timestamp = if (alreadyAdded != null) {
            //_lastOpenedFile.emit(alreadyAdded.value)
            alreadyAdded.key
        } else {
            val fileNodeSeconds = FileNodeSeconds(relativePath, null)
            if (!isReadOnly) {
                //_lastOpenedFile.emit(fileNodeSeconds)
            }
            // Fix: Use existing map and add new file instead of creating new map
            val map = _openedFilesState.value.states.toMutableMap()
            val timestamp = Clock.System.now().toEpochMilliseconds()
            map[timestamp] = fileNodeSeconds
            _openedFilesState.emit(OpenedFiles(map))
            timestamp
        }
        return timestamp
    }

    @OptIn(ExperimentalTime::class)
    fun getTimestampByFileNode(fileNode: FileTreeNode): Long? {
        return getTimestampByFileNode(fileNode.getRelativePath())
    }

    @OptIn(ExperimentalTime::class)
    fun getTimestampByFileNode(relativePath: String): Long? {
        val map = _openedFilesState.value.states
        for (pair in map) {
            if (pair.value.fileNodeRelativePath == relativePath)
                return pair.key
        }
        return null
    }

    @OptIn(ExperimentalTime::class)
    fun getFileNodeByTimestamp(timestamp: Long): String? {
        val map = _openedFilesState.value.states
        return map[timestamp]?.fileNodeRelativePath
    }

//    @OptIn(ExperimentalTime::class)
//    suspend fun movedFile(oldFileNode: FileTreeNode, newFileNode: FileTreeNode) {
//        movedFile(oldFileNode.getRelativePath(), newFileNode)
//    }

    @OptIn(ExperimentalTime::class)
    suspend fun movedFile(oldRelativePath: String, newRelativePath: String) {
        val timestamp = getTimestampByFileNode(oldRelativePath)

        val map = _openedFilesState.value.states.toMutableMap()

        val lastFile = map[timestamp]
        if (lastFile != null) {
            val last = FileNodeSeconds(
                newRelativePath,
                refreshToken = nowInMs()
            )
            map.put(
                timestamp!!,
                last
            )
            //_lastOpenedFile.emit(last)
            _openedFilesState.emit(OpenedFiles(map))
        }
//        else {
//            val openedFiles = _openedFilesState.value.states.toMutableMap()
//            when (oldFileNode) {
//                is FileTreeNode.Directory -> {
//                    val foundFiles = _openedFilesState.value.states
//                        //.filter { d -> d.value.fileNode is FileTreeNode.File }
//                        .filter { d ->
//                            d.value.fileNodeRelativePath.contains(oldFileNode.getRelativePath())
//                        }
//                    for (item in foundFiles) {
//                        openedFiles[item.key] =
//                            item.value.copy(fileNodeRelativePath = newFileNode.getRelativePath())
//                    }
//                }
//
//                is FileTreeNode.File -> {
//                    //todo openFile(fileNode, isReadOnly = false)
//                }
//            }
//            _openedFilesState.emit(OpenedFiles(openedFiles))
//        }
    }
}