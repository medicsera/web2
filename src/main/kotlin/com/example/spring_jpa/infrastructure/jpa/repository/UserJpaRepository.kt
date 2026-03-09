package com.example.spring_jpa.infrastructure.jpa.repository

import com.example.spring_jpa.infrastructure.jpa.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}