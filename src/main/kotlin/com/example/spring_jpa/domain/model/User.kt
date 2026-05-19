package com.example.spring_jpa.domain.model

data class User(
    val id: Long = 0,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean = true,
    val password: String = "",
    val role: Role = Role.USER
)