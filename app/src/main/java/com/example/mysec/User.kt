package com.example.mysec

data class User(
    val id: String,
    val name: String,
    val password: String
)

// LoginDataModel 추가
data class LoginDataModel(
    val id : String,
    val name : String
)