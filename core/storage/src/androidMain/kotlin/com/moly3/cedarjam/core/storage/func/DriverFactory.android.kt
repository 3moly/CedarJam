package com.moly3.cedarjam.core.storage.func

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.resultBlock
import com.moly3.cedarjam.core.storage.Database

actual fun createSqlDriver(
    androidApplicationContext: AndroidApplicationContext?,
    dbPath: String
): ResultWrapper<SqlDriver, DatabaseError> {
    return resultBlock {
        ensure(androidApplicationContext is Context) { DatabaseError.WrongFile("android context is not provided") }
        val driver = AndroidSqliteDriver(
            Database.Schema,
            androidApplicationContext,
            cacheSize = 1,
            name = dbPath,
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // PRAGMA journal_mode returns a result, so use query instead of execSQL
                    db.query("PRAGMA journal_mode=OFF").use { cursor ->
                        if (cursor.moveToFirst()) {
                            // Optional: log the result
                            val mode = cursor.getString(0)
                            println("Journal mode set to: $mode")
                        }
                    }
                }
            }
        )
        driver
    }
}