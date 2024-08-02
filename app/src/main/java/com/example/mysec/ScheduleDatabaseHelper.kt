package com.example.mysec

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScheduleDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "schedule.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "schedules"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DATE_RANGE = "date_range"
        const val COLUMN_IS_ONLINE = "is_online"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_TITLE TEXT,"
                + "$COLUMN_DATE_RANGE TEXT,"
                + "$COLUMN_IS_ONLINE INTEGER)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertSchedule(title: String, dateRange: String, isOnline: Boolean): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE_RANGE, dateRange)
            put(COLUMN_IS_ONLINE, if (isOnline) 1 else 0)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun updateSchedule(id: Int, title: String, dateRange: String, isOnline: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE_RANGE, dateRange)
            put(COLUMN_IS_ONLINE, if (isOnline) 1 else 0)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(id.toString()))
    }

    fun getScheduleId(title: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_TITLE = ?",
            arrayOf(title),
            null,
            null,
            null
        )
        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        } else {
            -1
        }.also { cursor.close() }
    }

}
