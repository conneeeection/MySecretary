// ProjectDatabaseHelper.kt
package com.example.mysec

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "projects.db"
private const val DATABASE_VERSION = 1

class ProjectDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // 프로젝트 테이블 생성 쿼리 실행
        db.execSQL("CREATE TABLE projects (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, date_range TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 기존 테이블 삭제 후 다시 생성(버전 업그레이드 시)
        db.execSQL("DROP TABLE IF EXISTS projects")
        onCreate(db)
    }
}