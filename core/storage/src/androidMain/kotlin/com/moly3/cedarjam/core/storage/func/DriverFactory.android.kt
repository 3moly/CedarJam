package com.moly3.cedarjam.core.storage.func

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.moly3.cedarjam.core.domain.model.AndroidApplicationContext
import com.moly3.cedarjam.core.domain.model.ResultWrapper
import com.moly3.cedarjam.core.domain.model.ensure
import com.moly3.cedarjam.core.domain.model.error.DatabaseError
import com.moly3.cedarjam.core.domain.model.resultBlock

actual fun createSqlDriver(
    androidApplicationContext: AndroidApplicationContext?,
    dbPath: String,
    schema: SqlSchema<QueryResult.Value<Unit>>
): ResultWrapper<SqlDriver, DatabaseError> {
    return resultBlock<SqlDriver, DatabaseError>(onError = { DatabaseError.Error(it.toString()) }) {
        ensure(androidApplicationContext is Context) { DatabaseError.WrongFile("android context is not provided") }
        val driver = AndroidSqliteDriver(
            schema,
            androidApplicationContext,
            cacheSize = 1,
            name = dbPath,
            callback = object : AndroidSqliteDriver.Callback(schema) {
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