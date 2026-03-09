package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.CreateOrderRequest
import com.example.spring_jpa.application.dto.UpdateOrderStatusRequest
import com.example.spring_jpa.domain.model.Order
import com.example.spring_jpa.domain.model.OrderStatus
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.domain.port.OrderRepositoryPort
import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepositoryPort,
    private val userRepository: UserRepositoryPort,
    private val dishRepository: DishRepositoryPort
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Order? =
        orderRepository.findByIdWithRelations(id)

    @Transactional(readOnly = true)
    fun findByUserId(userId: Long, pageable: Pageable): Page<Order> =
        orderRepository.findByUserId(userId, pageable)

    @Transactional(readOnly = true)
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order> =
        orderRepository.findByStatus(status, pageable)

    fun create(request: CreateOrderRequest): Order {
        userRepository.findById(request.userId)
            ?: throw IllegalArgumentException("User not found: ${request.userId}")

        val dishes = request.dishIds.map { dishId ->
            dishRepository.findById(dishId)
                ?: throw IllegalArgumentException("Dish not found: $dishId")
        }

        val order = Order(
            userId = request.userId,
            dishIds = request.dishIds,
            status = OrderStatus.PENDING
        )

        return orderRepository.create(order)
    }

    fun updateStatus(id: Long, request: UpdateOrderStatusRequest): Order? {
        val existing = orderRepository.findById(id) ?: return null
        return orderRepository.updateStatus(id, request.status)
    }

    fun deleteById(id: Long): Boolean =
        orderRepository.deleteById(id)
}