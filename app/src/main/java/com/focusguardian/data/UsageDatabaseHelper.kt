package com.focusguardian.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FocusGuardianUsage.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_DAILY_USAGE = "daily_usage"
        const val COL_DATE = "date_key"
        const val COL_PACKAGE = "package_name"
        const val COL_DURATION = "total_duration"
        const val COL_LAUNCHES = "launch_count"

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun getTodayKey(): String {
            return DATE_FORMAT.format(Date())
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_DAILY_USAGE (
                $COL_DATE TEXT,
                $COL_PACKAGE TEXT,
                $COL_DURATION INTEGER DEFAULT 0,
                $COL_LAUNCHES INTEGER DEFAULT 0,
                PRIMARY KEY ($COL_DATE, $COL_PACKAGE)
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DAILY_USAGE")
        onCreate(db)
    }

    fun addUsage(pkg: String, durationMs: Long) {
        val db = writableDatabase
        val date = getTodayKey()

        // Try update first
        val sql = "UPDATE $TABLE_DAILY_USAGE SET $COL_DURATION = $COL_DURATION + ? WHERE $COL_DATE = ? AND $COL_PACKAGE = ?"
        val statement = db.compileStatement(sql)
        statement.bindLong(1, durationMs)
        statement.bindString(2, date)
        statement.bindString(3, pkg)
        val updated = statement.executeUpdateDelete()

        if (updated == 0) {
            // Insert if not exists
            val values = ContentValues().apply {
                put(COL_DATE, date)
                put(COL_PACKAGE, pkg)
                put(COL_DURATION, durationMs)
                put(COL_LAUNCHES, 0) // Will be incremented separately preferably
            }
            db.insertWithOnConflict(TABLE_DAILY_USAGE, null, values, SQLiteDatabase.CONFLICT_IGNORE)
            
            // If conflict (race condition), run update again
            statement.executeUpdateDelete()
        }
    }

    fun incrementLaunch(pkg: String) {
        val db = writableDatabase
        val date = getTodayKey()

        val sql = "UPDATE $TABLE_DAILY_USAGE SET $COL_LAUNCHES = $COL_LAUNCHES + 1 WHERE $COL_DATE = ? AND $COL_PACKAGE = ?"
        val statement = db.compileStatement(sql)
        statement.bindString(1, date)
        statement.bindString(2, pkg)
        val updated = statement.executeUpdateDelete()

        if (updated == 0) {
            val values = ContentValues().apply {
                put(COL_DATE, date)
                put(COL_PACKAGE, pkg)
                put(COL_DURATION, 0)
                put(COL_LAUNCHES, 1)
            }
            db.insertWithOnConflict(TABLE_DAILY_USAGE, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }
    
    fun getDailyUsage(date: String): List<DailyUsageStat> {
        val list = mutableListOf<DailyUsageStat>()
        val db = readableDatabase
        val cursor = db.query(TABLE_DAILY_USAGE, null, "$COL_DATE = ?", arrayOf(date), null, null, "$COL_DURATION DESC")
        
        while (cursor.moveToNext()) {
            val pkg = cursor.getString(cursor.getColumnIndexOrThrow(COL_PACKAGE))
            val duration = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DURATION))
            val launches = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LAUNCHES))
            list.add(DailyUsageStat(pkg, duration, launches))
        }
        cursor.close()
        return list
    }
}

data class DailyUsageStat(val packageName: String, val durationMs: Long, val launchCount: Int)
