package com.example.spring_jpa.domain.port

import com.example.spring_jpa.domain.model.User

interface UserRepositoryPort {
    fun findAll(): List<User>
    fun findById(id: Long): User?
    fun create(user: User): User
    fun update(id: Long, user: User): User?
    fun deleteById(id: Long): Boolean
    fun existsByEmail(email: String): Boolean
}