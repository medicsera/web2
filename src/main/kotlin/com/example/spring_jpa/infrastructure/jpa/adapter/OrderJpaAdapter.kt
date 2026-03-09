package com.example.spring_jpa.infrastructure.jpa.adapter

import com.example.spring_jpa.domain.model.Order
import com.example.spring_jpa.domain.model.OrderStatus
import com.example.spring_jpa.domain.port.OrderRepositoryPort
import com.example.spring_jpa.infrastructure.jpa.entity.OrderEntity
import com.example.spring_jpa.infrastructure.jpa.repository.OrderJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class OrderJpaAdapter(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepositoryPort {

    override fun findAll(): List<Order> =
        orderJpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: Long): Order? =
        orderJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByIdWithRelations(id: Long): Order? =
        orderJpaRepository.findByIdWithRelations(id)?.toDomain()

    override fun findByUserId(userId: Long, pageable: Pageable): Page<Order> =
        orderJpaRepository.findByUserId(userId, pageable).map { it.toDomain() }

    override fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order> =
        orderJpaRepository.findByStatus(status, pageable).map { it.toDomain() }

    override fun save(order: Order): Order {
        val entity = OrderEntity.fromDomain(order)
        return orderJpaRepository.save(entity).toDomain()
    }

    override fun create(order: Order): Order {
        val entity = OrderEntity.fromDomain(order.copy(id = 0))
        return orderJpaRepository.save(entity).toDomain()
    }

    override fun updateStatus(id: Long, status: OrderStatus): Order? {
        val existing = orderJpaRepository.findById(id).orElse(null) ?: return null
        val updated = OrderEntity(
            id = existing.id,
            status = status,
            createdAt = existing.createdAt,
            user = existing.user,
            dishes = existing.dishes
        )
        return orderJpaRepository.save(updated).toDomain()
    }

    override fun deleteById(id: Long): Boolean {
        if (!orderJpaRepository.existsById(id)) return false
        orderJpaRepository.deleteById(id)
        return true
    }
}