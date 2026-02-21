package com.moly3.cedarjam.core.domain.func


const val hiddenDirectory: String = "moly3"
const val ignoredDsStoreFile: String = ".DS_Store"
const val ignoredFont: String = "${hiddenDirectory}/default.otf"
const val ignoredImportArchive: String = "${hiddenDirectory}/import.zip"
const val ignoredExportArchive: String = "${hiddenDirectory}/export.zip"
const val ignoredExportShareArchive: String = "${hiddenDirectory}/exportShare.zip"
const val sqlDatabaseName = "sqlite"
const val indexSqlDatabaseName = "indexes"
const val imgCache = "img_cache"
const val ignoreIndexSqlDatabaseName: String = "${hiddenDirectory}/$indexSqlDatabaseName.db"
const val ignoredDbShm: String = "${hiddenDirectory}/$sqlDatabaseName.db-shm"
const val ignoredDbWal: String = "${hiddenDirectory}/$sqlDatabaseName.db-wal"
const val ignoredDbJournal: String = "${hiddenDirectory}/$sqlDatabaseName.db-journal"
//const val ignoredImgCacheJournal: String = "${hiddenDirectory}/$imgCache/journal"

val ignoreByRelativePath = listOf(
//    ignoredFont,
    ignoredImportArchive,
    ignoredExportArchive,
    ignoredExportShareArchive,
    ignoredDbShm,
    ignoredDbWal,
    ignoredDbJournal,
    ignoreIndexSqlDatabaseName
)

val ignoreSearchByRelativePath = listOf(
    "${hiddenDirectory}/$sqlDatabaseName.db",
    "${hiddenDirectory}/workspace_settings.json"
)