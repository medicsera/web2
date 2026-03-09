package com.example.spring_jpa.domain.model

import java.time.LocalDateTime

data class Order(
    val id: Long = 0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val userId: Long,
    val dishIds: List<Long> = emptyList()
)