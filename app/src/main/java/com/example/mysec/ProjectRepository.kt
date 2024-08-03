package com.example.mysec

import android.content.ContentValues
import android.content.Context

class ProjectRepository(context: Context) {

    private val dbHelper = ProjectDatabaseHelper(context)

    // 프로젝트 삽입 메서드
    fun insertProject(title: String, dateRange: String, userId: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ProjectDatabaseHelper.COLUMN_TITLE, title)
            put(ProjectDatabaseHelper.COLUMN_DATE_RANGE, dateRange)
            put(ProjectDatabaseHelper.COLUMN_USER_ID, userId)
        }
        db.insert(ProjectDatabaseHelper.TABLE_NAME, null, values)
    }

    // 최근 프로젝트 제목 두 글자 가져오기
    fun getLatestProjectInitials(userId: String): String? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            ProjectDatabaseHelper.TABLE_NAME,
            arrayOf(ProjectDatabaseHelper.COLUMN_TITLE),
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "${ProjectDatabaseHelper.COLUMN_ID} DESC",
            "1"
        )
        val result = if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(ProjectDatabaseHelper.COLUMN_TITLE))
            cursor.close()
            title.take(2) // 제목의 첫 두 글자 반환
        } else {
            cursor.close()
            null
        }
        return result
    }

    // 최근 프로젝트의 제목, 날짜 범위, ID, 사용자 ID 가져오기
    fun getLatestProject(userId: String): Project? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            ProjectDatabaseHelper.TABLE_NAME,
            arrayOf(
                ProjectDatabaseHelper.COLUMN_ID,
                ProjectDatabaseHelper.COLUMN_TITLE,
                ProjectDatabaseHelper.COLUMN_DATE_RANGE,
                ProjectDatabaseHelper.COLUMN_USER_ID
            ),
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "${ProjectDatabaseHelper.COLUMN_ID} DESC",
            "1"
        )
        val result = if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(ProjectDatabaseHelper.COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(ProjectDatabaseHelper.COLUMN_TITLE))
            val dateRange = cursor.getString(cursor.getColumnIndexOrThrow(ProjectDatabaseHelper.COLUMN_DATE_RANGE))
            val userId = cursor.getString(cursor.getColumnIndexOrThrow(ProjectDatabaseHelper.COLUMN_USER_ID))
            cursor.close()
            Project(id, title, dateRange, userId) // Project 객체로 반환
        } else {
            cursor.close()
            null
        }
        return result
    }
}
