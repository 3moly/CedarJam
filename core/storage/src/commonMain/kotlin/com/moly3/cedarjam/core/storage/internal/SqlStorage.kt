package com.moly3.cedarjam.core.storage.internal

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.moly3.cedarjam.core.storage.ISqlStorage
import com.moly3.cedarjam.core.storage.ISystemFilesManager
import com.moly3.cedarjam.core.storage.exception.DbNotCreatedException
import com.moly3.cedarjam.core.storage.func.createSqlDriver
import com.moly3.cedarjam.core.domain.func.doNothing
import com.moly3.cedarjam.core.domain.func.hiddenDirectory
import com.moly3.cedarjam.core.domain.func.indexSqlDatabaseName
import com.moly3.cedarjam.core.domain.func.normalizeText
import com.moly3.cedarjam.core.domain.func.nowInMs
import com.moly3.cedarjam.core.domain.func.pathWrapper
import com.moly3.cedarjam.core.domain.func.sqlDatabaseName
import com.moly3.cedarjam.core.domain.func.toHexString
import com.moly3.cedarjam.core.domain.io
import com.moly3.cedarjam.core.domain.model.FileItem
import com.moly3.cedarjam.core.domain.model.FileTreeNode
import com.moly3.cedarjam.core.domain.model.IndexFileDto
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.SyncStatus
import com.moly3.cedarjam.core.domain.model.UIState
import com.moly3.cedarjam.core.domain.model.bind
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.ensureNotNull
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.fold
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.CreateCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.CreateTagToTagRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.RenameDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.RenameTagRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateDataCollectionRowRequest
import com.moly3.cedarjam.core.domain.model.request.UpdateTagRequest
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import com.moly3.cedarjam.core.storage.func.updateIndex
import com.moly3.cedarjam.core.storage.func.updateIndexLocal
import com.moly3.cedarjam.db.Annotation
import com.moly3.cedarjam.db.DataCollection
import com.moly3.cedarjam.db.DataCollectionRow
import com.moly3.cedarjam.db.Database
import com.moly3.cedarjam.db.Tag
import com.moly3.cedarjam.db.TagCollectionRow
import com.moly3.cedarjam.db.TagFileNode
import com.moly3.cedarjam.db.TagToTag
import com.moly3.cedarjam.indexdb.IndexDatabase
import com.moly3.cedarjam.indexdb.IndexFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.collections.listOf
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
internal class SqlStorage(
    private val systemFilesManager: ISystemFilesManager,
    private val applicationProvider: AppContextProvider,
    private val workspaceDirectoryPath: String
) : ISqlStorage {
    private val mapContext = io.limitedParallelism(50)

    private val _dbStateFlow =
        MutableStateFlow<UIState<Database, DatabaseError>>(UIState.Loading)

    private val _indexDbStateFlow =
        MutableStateFlow<UIState<IndexDatabase, DatabaseError>>(UIState.Loading)

    private val dbFlow: Flow<Database?> = _dbStateFlow.map { uiState ->
        when (uiState) {
            is UIState.Error -> null
            is UIState.Loading -> null
            is UIState.Success -> uiState.data
        }
    }.flowOn(mapContext)

    private val indexDbFlow: Flow<IndexDatabase?> = _indexDbStateFlow.map { uiState ->
        when (uiState) {
            is UIState.Error -> null
            is UIState.Loading -> null
            is UIState.Success -> uiState.data
        }
    }

    private fun <T> runQueryOrThrow(body: (Database) -> T): T {
        val dbState = _dbStateFlow.value
        return when (val state = dbState) {
            is UIState.Error -> throw DbNotCreatedException(state.error.toString())
            UIState.Loading -> throw DbNotCreatedException("database is initialized")
            is UIState.Success -> body(dbState.data)
        }
    }

    private fun <T> runQueryOrThrowIndex(body: (IndexDatabase) -> T): T {
        val dbState = _indexDbStateFlow.value
        return when (val state = dbState) {
            is UIState.Error -> throw DbNotCreatedException(state.error.toString())
            UIState.Loading -> throw DbNotCreatedException("database is initialized")
            is UIState.Success -> body(dbState.data)
        }
    }

    private fun getDatabasePath(): String {
        return pathWrapper(
            workspaceDirectoryPath,
            hiddenDirectory,
            "$sqlDatabaseName.db"
        ).pathString
    }

    private fun getIndexDatabasePath(): String {
        return pathWrapper(
            workspaceDirectoryPath,
            hiddenDirectory,
            "$indexSqlDatabaseName.db"
        ).pathString
    }

    private fun getGenericSqlDriver(
        dbPath: String,
        schema: SqlSchema<QueryResult.Value<Unit>>
    ): ResultWrapper<SqlDriver, DatabaseError> {
        return resultBlock {
            val dbPath = systemFilesManager.toAbsoluteAppPath(pathWrapper(dbPath)).pathString
            ensure(systemFilesManager.isNodeExists(dbPath)) { DatabaseError.NotExist }
            val sqlResult = createSqlDriver(
                applicationProvider.getApplicationContext(), dbPath,
                schema
            )
            bind(sqlResult)
        }
    }

    private fun getMainDbDriver(): ResultWrapper<SqlDriver, DatabaseError> {
        return getGenericSqlDriver(dbPath = getDatabasePath(), schema = Database.Schema)
    }

    private fun getIndexDbDriver(): ResultWrapper<SqlDriver, DatabaseError> {
        return getGenericSqlDriver(dbPath = getIndexDatabasePath(), schema = IndexDatabase.Schema)
    }

    override fun getDatabaseStatus(): Flow<UIState<Unit, DatabaseError>> {
        return _dbStateFlow.map {
            it.mapState {
                UIState.Success(Unit)
            }
        }
    }

    private fun createIfNotCreated() {
        val dbPath = getDatabasePath()
        //todo if directory created with db name, need to double check
        if (!systemFilesManager.isNodeExists(dbPath)) {
            systemFilesManager.createNode(
                workspacePath = "",
                isDirectory = false,
                nodePath = dbPath,
                byteArray = null,
                isMustCreate = true
            )
        }
        val indexDb = getIndexDatabasePath()
        if (!systemFilesManager.isNodeExists(indexDb)) {
            systemFilesManager.createNode(
                workspacePath = "",
                isDirectory = false,
                nodePath = indexDb,
                byteArray = null,
                isMustCreate = true
            )
        }
    }

    override suspend fun createDatabase() {
        createIfNotCreated()
        val sqlDriver = getMainDbDriver()
        _dbStateFlow.emit(
            sqlDriver.fold(
                onFailure = { error ->
                    UIState.Error(error)
                },
                onSuccess = { driver ->
                    UIState.Success(Database(driver))
                }
            ))
        val indexSqlDriver = getIndexDbDriver()
        _indexDbStateFlow.emit(
            indexSqlDriver.fold(
                onFailure = { error ->
                    UIState.Error(error)
                },
                onSuccess = { driver ->
                    UIState.Success(IndexDatabase(driver))
                }
            ))
    }

    private var mainSqlDriver: SqlDriver? = null
    private var indexSqlDriver2: SqlDriver? = null

    override fun init() {
        createIfNotCreated()
        val sqlDriver = getMainDbDriver()
        _dbStateFlow.value = sqlDriver.fold(
            onFailure = { error ->
                UIState.Error(error)
            },
            onSuccess = { driver ->
                mainSqlDriver = driver
                UIState.Success(Database(driver))
            }
        )
        val indexSqlDriver = getIndexDbDriver()
        _indexDbStateFlow.value = (
                indexSqlDriver.fold(
                    onFailure = { error ->
                        UIState.Error(error)
                    },
                    onSuccess = { driver ->
                        indexSqlDriver2 = driver
                        UIState.Success(IndexDatabase(driver))
                    }
                ))
    }

    override fun close() {
        mainSqlDriver?.close()
    }

    private fun <T : Any> Query<T>.mapFlowList(): Flow<List<T>> {
        return this
            .asFlow()
            .map {
                try {
                    it.executeAsList()
                } catch (exc: Exception) {
                    listOf()
                }
            }
    }

    private fun  Query<Long>.mapFlowAsOneLong(): Flow<Long> {
        return this
            .asFlow()
            .map {
                it.executeAsOneOrNull() ?: 0L
            }
    }

    private fun <T : Any> Query<T>.mapFlowAsOneOrNull(): Flow<T?> {
        return this
            .asFlow()
            .map {
                try {
                    it.executeAsOneOrNull()
                } catch (exc: Exception) {
                    null
                }
            }
    }

    override fun getIndexFilesFlow(): Flow<List<IndexFile>> {
        return indexDbFlow.flatMapLatest { db ->
            if (db != null) {
                db.indexFileQueries
                    .selectAll()
                    .mapFlowList()
            } else {
                flowOf(listOf())
            }
        }
    }

    override fun getIndexFiles(): List<IndexFile> {
        return runQueryOrThrowIndex { db ->
            db.indexFileQueries.selectAll().executeAsList()
        }
    }

    override fun getTagToTagsFlow(): Flow<List<TagToTag>> {
        return dbFlow.flowOn(mapContext).flatMapLatest { db ->
            if (db != null) {
                db.tagToTagQueries
                    .selectAll()
                    .mapFlowList()
//                    .asFlow()
//                    .mapToList(mapContext)
            } else {
                flowOf(listOf())
            }
        }
    }

    override fun getTagsFlow(): Flow<List<Tag>> {
        return dbFlow.flatMapLatest { db ->
            if (db != null) {
                db.tagQueries
                    .selectAll()
                    .mapFlowList()
//                    .asFlow()
//                    .mapToList(mapContext)
            } else {
                flowOf(listOf())
            }
        }
    }

    override fun getAnnotationsFlow(): Flow<List<Annotation>> {
        return dbFlow.flatMapLatest { db ->
            if (db != null) {
                db.annotationQueries
                    .selectAll()
                    .mapFlowList()
//                    .asFlow()
//                    .mapToList(mapContext)
            } else {
                flowOf(listOf())
            }
        }
    }


    override fun getTagFlow(id: Long): Flow<Tag?> {
        return dbFlow.flatMapLatest { db ->
            if (db != null) {
                db.tagQueries
                    .findOne(id = id)
                    .mapFlowAsOneOrNull()
//                    .asFlow()
//                    .mapToOneOrNull(mapContext)
            } else {
                flowOf(null)
            }
        }
    }

    override fun getTagLinks(): Flow<List<TagFileNode>> {
        return dbFlow.flatMapLatest {
            if (it != null) {
                it.tagFileNodeQueries
                    .selectAll()
                    .mapFlowList()
//                    .asFlow()
//                    .mapToList(mapContext)
            } else {
                flowOf(listOf())
            }
        }
    }

    override fun getCollections(): Flow<List<DataCollection>> {
        return dbFlow.flatMapLatest {
            it?.dataCollectionQueries
                ?.selectAll()
                ?.mapFlowList() ?: flowOf(listOf())
//                ?.asFlow()
//                ?.mapToList(mapContext) ?: flowOf(listOf())
        }
    }

    override fun getCollection(id: Long): Flow<DataCollection?> {
        return dbFlow.flatMapLatest {
            it?.dataCollectionQueries
                ?.selectById(id = id)
                ?.mapFlowAsOneOrNull() ?: flowOf(null)
//                ?.asFlow()
//                ?.mapToOneOrNull(mapContext) ?: flowOf(null)
        }
    }

    override fun getTagCollectionRows(): Flow<List<TagCollectionRow>> {
        return dbFlow.flatMapLatest {
            it?.tagCollectionRowQueries
                ?.selectAll()
                ?.mapFlowList() ?: flowOf(listOf())
//                ?.asFlow()
//                ?.mapToList(mapContext) ?: flowOf(listOf())
        }
    }

    override fun getCollectionRows(collectionId: Long?): Flow<List<DataCollectionRow>> {
        return dbFlow.flatMapLatest { db ->
            if (db != null) {
                val prepare = if (collectionId != null)
                    db.dataCollectionRowQueries
                        .selectByCollectionId(collectionId)
                else
                    db.dataCollectionRowQueries.selectAll()

                prepare.mapFlowList()
//                prepare.asFlow()
//                    .mapToList(mapContext)
            } else {
                flowOf(listOf())
            }
        }
    }

    override fun getCollectionRowsCount(collectionId: Long?): Flow<Long> {
        return dbFlow.flatMapLatest {
            it?.dataCollectionRowQueries
                ?.let {
                    if (collectionId != null) {
                        it.getCountByCollectionId(collectionId)
                    } else {
                        it.getCount()
                    }
                }
                ?.mapFlowAsOneLong() ?: flowOf(0L)
//                ?.asFlow()
//                ?.mapToOne(mapContext) ?: flowOf(0)
        }
    }

    override fun getCollectionRowsPaginated(
        offset: Long,
        pageSize: Long,
        collectionId: Long
    ): Flow<List<DataCollectionRow>> {
        return dbFlow.flatMapLatest { db ->
            if (db != null) {
                db.dataCollectionRowQueries
                    .getPaginatedItemsByCollectionId(
                        id = collectionId,
                        pageSize = pageSize,
                        offset = offset
                    )
                    .mapFlowList()
//                    .asFlow()
//                    .mapToList(mapContext)
            } else {
                flowOf(listOf())
            }
        }
    }

    override fun getCollectionRow(rowId: Long): Flow<DataCollectionRow?> {
        return dbFlow.flatMapLatest {
            if (it != null) {
                it.dataCollectionRowQueries
                    .selectById(id = rowId)
                    .mapFlowAsOneOrNull()
//                    .asFlow()
//                    .mapToOneOrNull(mapContext)
            } else {
                flowOf(null)
            }
        }
    }

    override fun updateIndexFilesLocal(localNodes: List<FileTreeNode>): ResultWrapper<Unit, String> {
        return runQueryOrThrowIndex { db ->
            resultBlock {
                updateIndexLocal(
                    localNodes,
                    db
                )
            }
        }
    }

    override fun updateIndexFiles(
        localNodes: List<FileTreeNode>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String> {
        return runQueryOrThrowIndex { db ->
            resultBlock {
                updateIndex(
                    localNodes,
                    serverNodes,
                    db
                )
            }
        }
    }

    override fun syncDirtyFiles(list: List<IndexFileDto>): ResultWrapper<Unit, String> {
        return runQueryOrThrowIndex { db ->
            resultBlock {
                db.indexFileQueries.selectAll().executeAsList().map {
                    val found = list.firstOrNull { b -> b.relativePath == it.relativePath }
                    if (found != null) {
                        db.indexFileQueries.updateLastSyncHash(
                            relativePath = found.relativePath,
                            lastSyncedHash = found.contentHash,
                            serverSyncStatus = 0L
                        )
                    }
                }
            }
        }
    }


    override fun deleteIndexFiles(list: List<String>): ResultWrapper<Unit, String> {
        return runQueryOrThrowIndex { db ->
            resultBlock {
                db.indexFileQueries.transaction {
                    for (item in list) {
                        db.indexFileQueries.deleteItem(item)
                    }
                }
            }
        }
    }

    override fun addIndexFiles(list: Map<String, Long>): ResultWrapper<Unit, String> {
        return runQueryOrThrowIndex { db ->
            resultBlock {
                db.indexFileQueries.transaction {
                    for (item in list) {
                        val absolute = pathWrapper(workspaceDirectoryPath, item.key).pathString
                        val meta = SystemFileSystem.metadataOrNull(Path(absolute))
                        db.indexFileQueries.insertItem(
                            relativePath = item.key,
                            contentHash = null,
                            modifiedTime = nowInMs(),
                            size = 0L,
                            lastSyncedHash = null,
                            serverSyncStatus = SyncStatus.DELETED.code,
                            isDirectory = if (meta?.isDirectory ?: false) 1L else 0L
                        )
                    }
                }
            }
        }
    }

    override fun createAnnotation(annotation: Annotation): ResultWrapper<Long, String> {
        return runQueryOrThrow { db ->
            resultBlock {
                db.annotationQueries.insertNew(
                    annotation
                )
                0L
            }
        }
    }

    override fun setFilesAsSynced(
        paths: List<String>,
        serverNodes: List<FileItem>
    ): ResultWrapper<Unit, String> {
        return runQueryOrThrowIndex { db ->
            resultBlock {
                com.moly3.cedarjam.core.storage.func.setFilesAsSynced(
                    paths,
                    serverNodes,
                    db,
                )
            }
        }
    }

    override fun createCollection(request: CreateCollectionRequest): ResultWrapper<Long, String> {
        return runQueryOrThrow { db ->
            resultBlock {
                val db = db.transactionWithResult {
                    db.dataCollectionQueries.insertObject(
                        DataCollection(
                            id = 0L,
                            name = request.name,
                            viewType = 0,
                            createdTime = request.createdTime,
                            modifiedTime = request.createdTime,
                        )
                    )
                    val id = db.dataCollectionQueries.lastInsertId().executeAsOneOrNull()
                    id!!
                }
                db
            }
        }
    }

    override fun createCollectionRow(request: CreateCollectionRowRequest): ResultWrapper<Long, String> {
        return runQueryOrThrow { db ->
            db.transactionWithResult {
                resultBlock {
                    ensure(request.name.isNotEmpty()) { "collection row - name is empty" }

                    val existedRows = db.dataCollectionRowQueries.searchByCollectionId(
                        name = request.name,
                        collectionId = request.collectionId
                    ).executeAsOne()
                    ensure(existedRows == 0L) { "row with this name is exists" }

                    db.dataCollectionRowQueries.insertObject(
                        DataCollectionRow = DataCollectionRow(
                            id = 0L,
                            name = request.name,
                            collectionId = request.collectionId,
                            createdTime = request.createdTime,
                            modifiedTime = request.createdTime,
                            currentProgress = request.currentProgress,
                            progressMax = request.progressMax,
                            webLink = request.webLink,
                            fileRelativePath = request.fileRelativePath,
                            imgRelativePath = request.imgRelativePath,
                            translation = request.translation,
                            exampleSentence = request.exampleSentence,
                            pronunciation = request.pronunciation,
                            isCompleted = if (request.isCompleted) 1L else 0L
                        )
                    )
                    db.dataCollectionRowQueries.lastInsertId().executeAsOne()
                }
            }
        }
    }

    override fun createTag(tag: Tag): ResultWrapper<Long, String> {
        return runQueryOrThrow { db ->
            resultBlock {
                db.transactionWithResult {
                    val foundTag = db.tagQueries.search(tag.name).executeAsList()
                    ensure(foundTag.isEmpty()) { "there is already tag with this name: ${tag.name}" }
                    db.tagQueries.insertObject(tag)
                    val tagId = db.tagQueries.lastInsertId().executeAsOneOrNull()
                    ensureNotNull(tagId) { "tag id is null" }
                }
            }
        }
    }

    override fun updateTag(request: UpdateTagRequest): ResultWrapper<Unit, String> {
        return runQueryOrThrow { db ->
            resultBlock {
                val foundTagId = db.tagQueries.findOneForId(request.id).executeAsOneOrNull()
                ensureNotNull(foundTagId) { "when updating tag. tag is not exists (id: ${foundTagId})" }
                db.tagQueries.update(
                    id = request.id,
                    color = request.color.toHexString(),
                    modifiedTime = request.modifiedTime
                )
            }
        }
    }

    override fun createTagToTag(request: CreateTagToTagRequest): ResultWrapper<Long, String> {
        return runQueryOrThrow { db ->
            resultBlock {
                db.transactionWithResult {
                    val tags =
                        db.tagToTagQueries.search(request.tagId, request.tag2Id).executeAsList()
                    ensure(tags.isEmpty()) { "" }

                    db.tagToTagQueries.insertObject(
                        TagToTag(
                            id = 1L,
                            firstTagId = request.tagId,
                            secondTagId = request.tag2Id,
                            createdTime = request.modifiedTime
                        )
                    )
                    val tagId = db.tagToTagQueries.lastInsertId().executeAsOneOrNull()
                    tagId!!
                }
            }
        }
    }

    override fun addTagLink(relativePath: String, tagId: Long) {
        runQueryOrThrow { db ->
            db.tagFileNodeQueries.insertObject(
                TagFileNode(
                    id = 1L,
                    tagId = tagId,
                    fileNodeRelativePath = relativePath,
                    createdTime = nowInMs()
                )
            )
        }
    }

    override fun createTagCollectionRow(request: CreateTagCollectionRowRequest) {
        runQueryOrThrow { db ->
            db.tagCollectionRowQueries.insertObject(
                TagCollectionRow(
                    id = 0L,
                    tagId = request.tagId,
                    rowId = request.rowId,
                    createdTime = request.createdTime
                )
            )
        }
    }

    override fun renameTag(request: RenameTagRequest) {
        runQueryOrThrow { db ->
            val found = db.tagQueries.search(request.newName).executeAsOneOrNull()
            if (found != null) {

            } else {
                db.tagQueries.rename(
                    id = request.id,
                    newName = request.newName,
                    modifiedTime = request.modifiedTime
                )
            }
        }
    }


    override fun renameFileNode(oldRelativePath: String, newRelativePath: String) {
        runQueryOrThrow { db ->
            db.transaction {
                db.dataCollectionRowQueries.updateFile(
                    oldFileRelativePath = oldRelativePath,
                    newFileRelativePath = newRelativePath
                )
                db.dataCollectionRowQueries.updateImg(
                    oldFileRelativePath = oldRelativePath,
                    newFileRelativePath = newRelativePath
                )
                db.tagFileNodeQueries.updateFile(
                    oldFileRelativePath = oldRelativePath,
                    newFileRelativePath = newRelativePath
                )
                val all = db.annotationQueries.selectAll().executeAsList()
                val physicalFounds =
                    all.filter { d -> d.dataPath.normalizeText() == oldRelativePath.normalizeText() }

                for (item in physicalFounds) {
                    db.annotationQueries.updateDataPathById(
                        newFileRelativePath = newRelativePath.normalizeText(),
                        id = item.id
                    )
                }
            }
        }
    }

    override fun renameCollectionRow(request: RenameDataCollectionRowRequest) {
        runQueryOrThrow { db ->
            println("renameCollectionRow ${request.newName}")
            if (request.newName.isEmpty()) {
                doNothing()
            } else {
                db.dataCollectionRowQueries.rename(
                    id = request.rowId,
                    newName = request.newName,
                    modifiedTime = request.modifiedTime
                )
            }
        }

    }

    override fun renameCollection(request: RenameDataCollectionRequest) {
        runQueryOrThrow { db ->
            db.dataCollectionQueries.rename(
                id = request.id,
                newName = request.newName,
                modifiedTime = request.modifiedTime
            )
        }
    }

    override fun updateCollection(request: UpdateDataCollectionRequest) {
        runQueryOrThrow { db ->
            db.dataCollectionQueries.updateViewType(
                viewType = request.viewType.num,
                modifiedTime = request.modifiedTime,
                id = request.id
            )
        }
    }

    override fun updateCollectionRow(request: UpdateDataCollectionRowRequest) {
        runQueryOrThrow { db ->
            db.dataCollectionRowQueries.updateObject(
                id = request.id,
                currentProgress = request.currentProgress,
                progressMax = request.progressMax,
                webLink = request.webLink,
                fileRelativePath = request.fileRelativePath,
                imgRelativePath = request.imgRelativePath,
                isCompleted = if (request.isCompleted) 1L else 0L,

                translation = request.translation,
                exampleSentence = request.exampleSentence,
                pronunciation = request.pronunciation,

                modifiedTime = request.modifiedTime,
            )
        }
    }

    override fun deleteTag(id: Long) {
        runQueryOrThrow { db ->
            db.tagQueries.delete(id = id)
        }
    }

    override fun deleteTagLink(id: Long) {
        runQueryOrThrow { db ->
            db.tagFileNodeQueries.delete(id = id)
        }
    }

    override fun deleteCollectionRow(id: Long) {
        runQueryOrThrow { db ->
            db.dataCollectionRowQueries.delete(id = id)
        }
    }

    override fun deleteCollection(id: Long) {
        runQueryOrThrow { db ->
            db.dataCollectionQueries.delete(id = id)
        }
    }

    override fun deleteAnnotation(id: Long) {
        runQueryOrThrow { db ->
            db.annotationQueries.delete(id = id)
        }
    }

    override fun deleteTagToTag(id: Long) {
        runQueryOrThrow { db ->
            db.tagToTagQueries.delete(id = id)
        }
    }

    override fun deleteTagCollectionRow(id: Long) {
        runQueryOrThrow { db ->
            db.tagCollectionRowQueries.delete(id = id)
        }
    }
}