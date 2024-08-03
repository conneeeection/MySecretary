package com.example.mysec

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ProjectDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "projects.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "projects"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DATE_RANGE = "date_range"
        const val COLUMN_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TITLE TEXT, " +
                "$COLUMN_DATE_RANGE TEXT, " +
                "$COLUMN_USER_ID TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // 프로젝트 추가 메서드
    fun addProject(title: String, dateRange: String, userId: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATE_RANGE, dateRange)
            put(COLUMN_USER_ID, userId)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }

    // 프로젝트 조회 메서드
    fun getProjects(userId: String): List<Project> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_USER_ID = ?", arrayOf(userId))
        val projects = mutableListOf<Project>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val dateRange = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_RANGE))
                projects.add(Project(id, title, dateRange, userId))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return projects
    }
}
