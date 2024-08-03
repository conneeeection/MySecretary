package com.example.mysec

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getStringOrNull

class ScheduleRepository(context: Context) {

    private val dbHelper: ScheduleDatabaseHelper = ScheduleDatabaseHelper(context)

    fun getSchedules(userId: String): Pair<List<String>, Map<String, Pair<String, Boolean>>> {
        val scheduleList = mutableListOf<String>()
        val scheduleMap = mutableMapOf<String, Pair<String, Boolean>>()
        val db = dbHelper.readableDatabase

        val cursor: Cursor? = try {
            db.query(
                ScheduleDatabaseHelper.TABLE_NAME,
                null,
                "user_id = ?",
                arrayOf(userId),
                null,
                null,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        cursor?.use {
            while (it.moveToNext()) {
                val title = it.getStringOrNull(it.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_TITLE)) ?: ""
                val dateRange = it.getStringOrNull(it.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_DATE_RANGE)) ?: ""
                val isOnline = it.getInt(it.getColumnIndexOrThrow(ScheduleDatabaseHelper.COLUMN_IS_ONLINE)) == 1
                scheduleList.add(title)
                scheduleMap[title] = Pair(dateRange, isOnline)
            }
        }

        return Pair(scheduleList, scheduleMap)
    }

    fun getScheduleId(title: String): Int {
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = try {
            db.query(
                ScheduleDatabaseHelper.TABLE_NAME,
                arrayOf("id"),
                "title = ?",
                arrayOf(title),
                null,
                null,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val id = cursor?.use {
            if (it.moveToFirst()) {
                it.getInt(it.getColumnIndexOrThrow("id"))
            } else {
                -1
            }
        }

        return id ?: -1
    }

    fun insertSchedule(title: String, dateRange: String, isOnline: Boolean): Long {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(ScheduleDatabaseHelper.COLUMN_TITLE, title)
            put(ScheduleDatabaseHelper.COLUMN_DATE_RANGE, dateRange)
            put(ScheduleDatabaseHelper.COLUMN_IS_ONLINE, if (isOnline) 1 else 0)
        }
        return try {
            db.insert(ScheduleDatabaseHelper.TABLE_NAME, null, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    fun updateSchedule(id: Int, title: String, dateRange: String, isOnline: Boolean): Int {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(ScheduleDatabaseHelper.COLUMN_TITLE, title)
            put(ScheduleDatabaseHelper.COLUMN_DATE_RANGE, dateRange)
            put(ScheduleDatabaseHelper.COLUMN_IS_ONLINE, if (isOnline) 1 else 0)
        }
        return try {
            db.update(
                ScheduleDatabaseHelper.TABLE_NAME,
                contentValues,
                "id = ?",
                arrayOf(id.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun deleteSchedule(id: Int): Int {
        val db = dbHelper.writableDatabase
        return try {
            db.delete(
                ScheduleDatabaseHelper.TABLE_NAME,
                "id = ?",
                arrayOf(id.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
