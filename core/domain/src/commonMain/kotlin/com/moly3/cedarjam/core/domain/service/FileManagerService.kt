package com.moly3.cedarjam.core.domain.service

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
        val fileNodeFullPath: String = "",
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
        return openFile(fileNode.getFullPath(), isReadOnly)
    }

    suspend fun openFile(fullpath: String, isReadOnly: Boolean): Long {
        val alreadyAdded = _openedFilesState.value
            .states
            .asSequence()
            .firstOrNull { d -> d.value.fileNodeFullPath == fullpath }
        val timestamp = if (alreadyAdded != null) {
            //_lastOpenedFile.emit(alreadyAdded.value)
            alreadyAdded.key
        } else {
            val fileNodeSeconds = FileNodeSeconds(fullpath, null)
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
        return getTimestampByFileNode(fileNode.getFullPath())
    }

    @OptIn(ExperimentalTime::class)
    fun getTimestampByFileNode(fullpath: String): Long? {
        val map = _openedFilesState.value.states
        for (pair in map) {
            if (pair.value.fileNodeFullPath == fullpath)
                return pair.key
        }
        return null
    }

    @OptIn(ExperimentalTime::class)
    fun getFileNodeByTimestamp(timestamp: Long): String? {
        val map = _openedFilesState.value.states
        return map[timestamp]?.fileNodeFullPath
    }

    @OptIn(ExperimentalTime::class)
    suspend fun movedFile(oldFileNode: FileTreeNode, newFileNode: FileTreeNode) {
        movedFile(oldFileNode.getFullPath(), newFileNode)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun movedFile(oldFileNode: String, newFileNode: FileTreeNode) {
        val timestamp = getTimestampByFileNode(oldFileNode)

        val map = _openedFilesState.value.states.toMutableMap()

        val lastFile = map[timestamp]
        if (lastFile != null) {
            val last = FileNodeSeconds(
                newFileNode.getFullPath(),
                refreshToken = Clock.System.now().toEpochMilliseconds()
            )
            map.put(
                timestamp!!,
                last
            )
            //_lastOpenedFile.emit(last)
            _openedFilesState.emit(OpenedFiles(map))
        } else {
            val openedFiles = _openedFilesState.value.states.toMutableMap()
            when (oldFileNode) {
                is FileTreeNode.Directory -> {
                    val foundFiles = _openedFilesState.value.states
                        //.filter { d -> d.value.fileNode is FileTreeNode.File }
                        .filter { d ->
                            d.value.fileNodeFullPath.contains(oldFileNode.getFullPath())
                        }
                    for (item in foundFiles) {
                        val fileNode = item.value.fileNodeFullPath
                        val newParentPath = fileNode.replaceFirst(
                            oldFileNode.getFullPath(),
                            newFileNode.getFullPath()
                        )
                        openedFiles[item.key] =
                            item.value.copy(fileNodeFullPath = newParentPath)
                    }
                }

                is FileTreeNode.File -> {
                    //todo openFile(fileNode, isReadOnly = false)
                }
            }
            _openedFilesState.emit(OpenedFiles(openedFiles))
        }
    }
}