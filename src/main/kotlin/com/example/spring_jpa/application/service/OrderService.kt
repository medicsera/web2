package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.CreateOrderRequest
import com.example.spring_jpa.application.dto.UpdateOrderStatusRequest
import com.example.spring_jpa.application.exception.BadRequestException
import com.example.spring_jpa.application.exception.InvalidOrderStateException
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.domain.model.Order
import com.example.spring_jpa.domain.model.OrderStatus
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.domain.port.OrderRepositoryPort
import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findById(id: Long): Order {
        return orderRepository.findByIdWithRelations(id)
            ?: run {
                logger.warn("Order not found with id: {}", id)
                throw NotFoundException("Order not found with id: $id")
            }
    }

    @Transactional(readOnly = true)
    fun findByUserId(userId: Long, pageable: Pageable): Page<Order> =
        orderRepository.findByUserId(userId, pageable)

    @Transactional(readOnly = true)
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order> =
        orderRepository.findByStatus(status, pageable)

    @Transactional(readOnly = true)
    fun findAll(userId: Long?, status: OrderStatus?): List<Order> {
        var orders = orderRepository.findAll()
        if (userId != null) orders = orders.filter { it.userId == userId }
        if (status != null) orders = orders.filter { it.status == status }
        return orders
    }

    fun create(request: CreateOrderRequest): Order {
        val userId = request.userId!!
        val dishIds = request.dishIds!!
        userRepository.findById(userId)
            ?: run {
                logger.warn("User not found with id: {}", userId)
                throw BadRequestException("User not found with id: $userId")
            }
        dishIds.forEach { dishId ->
            dishRepository.findById(dishId)
                ?: run {
                    logger.warn("Dish not found with id: {}", dishId)
                    throw BadRequestException("Dish not found with id: $dishId")
                }
        }
        val order = Order(
            userId = userId,
            dishIds = dishIds,
            status = OrderStatus.PENDING
        )
        val created = orderRepository.create(order)
        logger.info("Created order with id: {}", created.id)
        return created
    }

    fun updateStatus(id: Long, request: UpdateOrderStatusRequest): Order {
        val existing = orderRepository.findById(id)
            ?: run {
                logger.warn("Order not found with id: {}", id)
                throw NotFoundException("Order not found with id: $id")
            }
        validateStatusTransition(existing.status, request.status)
        val updated = orderRepository.updateStatus(id, request.status)
            ?: throw NotFoundException("Order not found with id: $id")
        logger.info("Updated order {} status from {} to {}", id, existing.status, request.status)
        return updated
    }

    fun deleteById(id: Long) {
        val deleted = orderRepository.deleteById(id)
        if (!deleted) {
            logger.warn("Order not found with id: {}", id)
            throw NotFoundException("Order not found with id: $id")
        }
        logger.info("Deleted order with id: {}", id)
    }

    private fun validateStatusTransition(current: OrderStatus, next: OrderStatus) {
        val allowed = when (current) {
            OrderStatus.PENDING -> setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED)
            OrderStatus.CONFIRMED -> setOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED)
            OrderStatus.DELIVERED, OrderStatus.CANCELLED -> emptySet()
        }
        if (next !in allowed) {
            throw InvalidOrderStateException(
                "Cannot transition order from $current to $next"
            )
        }
    }
}
