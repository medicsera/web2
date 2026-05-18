package com.example.spring_jpa.application.dto

import com.example.spring_jpa.domain.model.Order
import com.example.spring_jpa.domain.model.OrderStatus
import java.time.LocalDateTime

data class UpdateOrderStatusRequest(
    val status: OrderStatus
)

data class OrderResponse(
    val id: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val userId: Long,
    val dishes: List<DishResponse>
) {
    companion object {
        fun fromDomain(order: Order): OrderResponse = OrderResponse(
            id = order.id,
            status = order.status,
            createdAt = order.createdAt,
            userId = order.userId,
            dishes = order.dishes.map { DishResponse.fromDomain(it) }
        )
    }
}
