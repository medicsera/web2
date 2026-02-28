package com.example.spring_jpa.domain.model

import java.math.BigDecimal

data class Dish(
    val id: Long = 0,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val isAvailable: Boolean = true,
)
