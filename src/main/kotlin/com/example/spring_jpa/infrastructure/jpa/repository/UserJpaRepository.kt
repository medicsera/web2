package com.example.spring_jpa.infrastructure.jpa.repository

import com.example.spring_jpa.infrastructure.jpa.entity.UserEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = ["orders"])
    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    fun findWithOrdersById(@Param("id") id: Long): UserEntity?

    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}