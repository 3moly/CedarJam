package com.moly3.cedarjam.core.domain.func


const val hiddenDirectory: String = "moly3"
const val ignoredDsStoreFile: String = ".DS_Store"
val ignoredFont: String = pathWrapper(hiddenDirectory, "default.otf").pathString
val ignoredImportArchive: String = pathWrapper(hiddenDirectory, "import.zip").pathString
val ignoredExportArchive: String = pathWrapper(hiddenDirectory, "export.zip").pathString
val ignoredExportShareArchive: String = pathWrapper(hiddenDirectory, "exportShare.zip").pathString
const val sqlDatabaseName = "sqlite"
const val indexSqlDatabaseName = "indexes"
const val imgCache = "image_cache"
val ignoreIndexSqlDatabaseName: String = pathWrapper(hiddenDirectory, "$indexSqlDatabaseName.db").pathString
val ignoredDbShm: String = pathWrapper(hiddenDirectory, "$sqlDatabaseName.db-shm").pathString
val ignoredDbWal: String = pathWrapper(hiddenDirectory, "$sqlDatabaseName.db-wal").pathString
val ignoredDbJournal: String = pathWrapper(hiddenDirectory, "$sqlDatabaseName.db-journal").pathString

val ignoreByRelativePath = listOf(
    ignoredImportArchive,
    ignoredExportArchive,
    ignoredExportShareArchive,
    ignoredDbShm,
    ignoredDbWal,
    ignoredDbJournal,
    ignoreIndexSqlDatabaseName
)

val ignoreSearchByRelativePath = listOf(
    pathWrapper(hiddenDirectory, "$sqlDatabaseName.db").pathString,
    pathWrapper(hiddenDirectory, "workspace_settings.json").pathString,
    pathWrapper(hiddenDirectory, imgCache).pathString,
)