package com.moly3.cedarjam.core.domain.func


const val hiddenDirectory: String = "moly3"
const val ignoredDsStoreFile: String = ".DS_Store"
const val ignoredFont: String = "${hiddenDirectory}/default.otf"
const val ignoredImportArchive: String = "${hiddenDirectory}/import.zip"
const val ignoredExportArchive: String = "${hiddenDirectory}/export.zip"
const val sqlDatabaseName = "sqlite"
const val indexSqlDatabaseName = "indexes"
const val ignoreIndexSqlDatabaseName: String = "${hiddenDirectory}/$indexSqlDatabaseName.db"
const val ignoredDbShm: String = "${hiddenDirectory}/$sqlDatabaseName.db-shm"
const val ignoredDbWal: String = "${hiddenDirectory}/$sqlDatabaseName.db-wal"
const val ignoredDbJournal: String = "${hiddenDirectory}/$sqlDatabaseName.db-journal"

val ignoreByRelativePath = listOf(
    ignoredFont,
    ignoredImportArchive,
    ignoredExportArchive,
    ignoredDbShm,
    ignoredDbWal,
    ignoredDbJournal,
    ignoreIndexSqlDatabaseName
)