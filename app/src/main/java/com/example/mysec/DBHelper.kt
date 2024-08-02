package com.example.mysec

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Calendar
import java.util.Date

class DBHelper(context: Context) : SQLiteOpenHelper(context, DBNAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE users (
                id TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

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
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS events")
        onCreate(db)
    }

    fun insertData(id: String, password: String, name: String): Boolean {
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

    fun checkUser(id: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(id))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun checkUserpass(id: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE id = ? AND password = ?",
            arrayOf(id, password)
        )
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }

    fun getUserInfo(id: String): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(id))
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

    fun deleteUser(id: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete("users", "id = ?", arrayOf(id))
        db.close()
        return result > 0
    }

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

    fun getAllEvents(userId: String): List<Event> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM events WHERE user_id = ?", arrayOf(userId))
        val events = mutableListOf<Event>()
        if (cursor.moveToFirst()) {
            do {
                val eventDate = cursor.getLong(cursor.getColumnIndexOrThrow("event_date"))
                val event = cursor.getString(cursor.getColumnIndexOrThrow("event"))
                events.add(Event(userId, Date(eventDate), event))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

    fun getEventsForDate(userId: String, date: Long): List<Event> {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM events WHERE user_id = ? AND event_date BETWEEN ? AND ?",
            arrayOf(
                userId,
                getStartOfDay(date).toString(),
                getEndOfDay(date).toString()
            )
        )
        val events = mutableListOf<Event>()
        if (cursor.moveToFirst()) {
            do {
                val eventDate = cursor.getLong(cursor.getColumnIndexOrThrow("event_date"))
                val event = cursor.getString(cursor.getColumnIndexOrThrow("event"))
                events.add(Event(userId, Date(eventDate), event))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return events
    }

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

    fun addEvent(userId: String, eventDate: Date, event: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("user_id", userId)
            put("event_date", eventDate.time)
            put("event", event)
        }
        val result = db.insert("events", null, contentValues)
        db.close()
        return result != -1L
    }

    // 이벤트 삭제 메서드
    fun deleteEvent(userId: String, event: Event): Boolean {
        val db = writableDatabase
        return try {
            // 이벤트를 식별할 조건 설정 (userId와 date)
            val whereClause = "user_id = ? AND event_date = ? AND event = ?"
            val whereArgs = arrayOf(userId, event.date.time.toString(), event.event)
            val rowsAffected = db.delete("events", whereClause, whereArgs)
            rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    companion object {
        private const val DBNAME = "Mybiseo.db"
    }
}
