package com.example.spring_jpa.application.dto

import com.example.spring_jpa.domain.model.Order
import com.example.spring_jpa.domain.model.OrderStatus
import java.time.LocalDateTime

data class CreateOrderRequest(
    val userId: Long,
    val dishIds: List<Long>
)

data class UpdateOrderStatusRequest(
    val status: OrderStatus
)

data class OrderResponse(
    val id: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val userId: Long,
    val dishIds: List<Long>
) {
    companion object {
        fun fromDomain(order: Order): OrderResponse = OrderResponse(
            id = order.id,
            status = order.status,
            createdAt = order.createdAt,
            userId = order.userId,
            dishIds = order.dishIds
        )
    }
}