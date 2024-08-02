// ProjectRepository.kt
package com.example.mysec

import android.content.ContentValues
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectRepository(context: Context) {

    // SQLiteOpenhelper를 사용하여 데이터베이스 인스턴스 생성
    private val dbHelper = ProjectDatabaseHelper(context)

    // 프로젝트 데이터를 데이터베이스에 삽입하는 함수
    suspend fun insertProject(title: String, dateRange: String) {
        withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("title", title)
                put("date_range", dateRange)
            }
            // 데이터베이스에 삽입
            db.insert("projects", null, values)
        }
    }

    // 최근 프로젝트의 제목 두 글자를 가져오는 함수
    suspend fun getLatestProjectInitials(): String {
        return withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.query("projects", arrayOf("title"), null, null, null, null, "id DESC", "1")
            val initials = if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow("title")).take(2)
            } else {
                ""
            }
            cursor.close()
            initials
        }
    }
}