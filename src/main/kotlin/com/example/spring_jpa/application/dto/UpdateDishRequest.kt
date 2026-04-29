package com.example.spring_jpa.application.dto

data class UpdateDishRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val isAvailable: Boolean = true
)
