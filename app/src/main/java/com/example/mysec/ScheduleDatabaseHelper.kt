package com.example.mysec

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScheduleDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "schedule.db"
        private const val DATABASE_VERSION = 2 // 데이터베이스 버전 증가
        const val TABLE_NAME = "schedules"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DATE_RANGE = "date_range"
        const val COLUMN_IS_ONLINE = "is_online"
        const val COLUMN_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TITLE TEXT,"
                + "$COLUMN_DATE_RANGE TEXT,"
                + "$COLUMN_IS_ONLINE INTEGER,"
                + "$COLUMN_USER_ID TEXT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_USER_ID TEXT")
        }
    }

    // 일정 리스트 추가
    fun insertSchedule(title: String, dateRange: String, isOnline: Boolean, userId: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE_RANGE, dateRange)
            put(COLUMN_IS_ONLINE, if (isOnline) 1 else 0)
            put(COLUMN_USER_ID, userId) // user_id 추가
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // 일정 리스트 업데이트
    fun updateSchedule(id: Int, title: String, dateRange: String, isOnline: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE_RANGE, dateRange)
            put(COLUMN_IS_ONLINE, if (isOnline) 1 else 0)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(id.toString()))
    }
}
