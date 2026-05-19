package com.example.spring_jpa.infrastructure.jpa.repository

import com.example.spring_jpa.domain.model.OrderStatus
import com.example.spring_jpa.infrastructure.jpa.entity.OrderEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
    @EntityGraph(attributePaths = ["user", "dishes"])
    override fun findAll(): List<OrderEntity>

    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findByUserId(userId: Long, pageable: Pageable): Page<OrderEntity>

    @EntityGraph(attributePaths = ["user", "dishes"])
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<OrderEntity>

    @EntityGraph(attributePaths = ["user", "dishes"])
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :id")
    fun findByIdWithRelations(@Param("id") id: Long): OrderEntity?
}