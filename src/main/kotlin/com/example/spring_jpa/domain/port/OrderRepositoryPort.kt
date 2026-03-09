package com.example.spring_jpa.domain.port

import com.example.spring_jpa.domain.model.Order
import com.example.spring_jpa.domain.model.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrderRepositoryPort {
    fun findAll(): List<Order>
    fun findById(id: Long): Order?
    fun findByIdWithRelations(id: Long): Order?
    fun findByUserId(userId: Long, pageable: Pageable): Page<Order>
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order>
    fun save(order: Order): Order
    fun create(order: Order): Order
    fun updateStatus(id: Long, status: OrderStatus): Order?
    fun deleteById(id: Long): Boolean
}