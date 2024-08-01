package com.example.mysec

import java.util.Date

data class Event(
    val id: Int,
    val userId: String,
    val date: Date,
    val event: String
)