package com.example.mysec

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Calendar
import java.util.Date

class DBHelper(context: Context) : SQLiteOpenHelper(context, DBNAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // 사용자 테이블 생성
        val createUsersTable = """
            CREATE TABLE users (
                id TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        // 이벤트 테이블 생성
        val createEventsTable = """
            CREATE TABLE events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                event_date INTEGER NOT NULL,
                event TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createEventsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 테이블이 존재할 경우 삭제
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS events")
        onCreate(db)
    }

    // id, password, name 삽입 (성공 시 true, 실패 시 false)
    fun insertData(id: String?, password: String?, name: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("id", id)
            put("password", password)
            put("name", name)
        }
        val result = db.insert("users", null, contentValues)
        db.close()
        return result != -1L
    }

    // 사용자 아이디가 없으면 false, 이미 존재하면 true
    fun checkUser(id: String?): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(id))
        val exists = cursor.count > 0
        cursor.close() // 커서 닫기
        return exists
    }

    // 해당 id, password가 있는지 확인 (없다면 false)
    fun checkUserpass(id: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE id = ? AND password = ?",
            arrayOf(id, password)
        )
        val isValid = cursor.count > 0
        cursor.close() // 커서 닫기
        return isValid
    }

    // 사용자 정보를 가져오는 함수 (id는 String으로 변경)
    fun getUserInfo(id: String): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(id))
        Log.d("DBHelper", "Executing query for ID: $id") // 쿼리 실행 로그 추가
        var user: User? = null
        if (cursor.moveToFirst()) {
            val userId = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            user = User(userId, name, password)
        }
        cursor.close()
        return user
    }

    // 사용자 삭제 메서드 추가
    fun deleteUser(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete("users", "id = ?", arrayOf(id))
        db.close()
        return result > 0
    }

    // 사용자 이름 업데이트
    fun updateUserName(userId: String, newName: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", newName)
        }
        val result = db.update(
            "users",
            contentValues,
            "id = ?",
            arrayOf(userId)
        )
        db.close()
        return result > 0
    }

    // 모든 이벤트 가져오기
    fun getAllEvents(userId: String): List<Event> {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM events WHERE user_id = ?", arrayOf(userId))
        val events = mutableListOf<Event>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val eventDate = cursor.getLong(cursor.getColumnIndexOrThrow("event_date"))
                val event = cursor.getString(cursor.getColumnIndexOrThrow("event"))
                events.add(Event(id, userId, Date(eventDate), event))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

    // 특정 날짜에 대한 이벤트 가져오기
    fun getEventsForDate(userId: String, date: Long): List<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT event FROM events WHERE user_id = ? AND event_date BETWEEN ? AND ?",
            arrayOf(
                userId,
                getStartOfDay(date).toString(),
                getEndOfDay(date).toString()
            )
        )
        val events = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            do {
                val event = cursor.getString(cursor.getColumnIndexOrThrow("event"))
                events.add(event)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

    // 날짜의 시작 시간을 밀리초로 가져오기
    private fun getStartOfDay(dateMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // 날짜의 끝 시간을 밀리초로 가져오기
    private fun getEndOfDay(dateMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val DBNAME = "Mybiseo.db" // 데이터베이스 이름을 Mybiseo.db로 변경
    }
}