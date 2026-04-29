package com.example.spring_jpa.application.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size


data class CreateOrderRequest(

    @field:NotNull(message = "User ID cannot be null")
    val userId: Long,

    @field:NotNull(message = "Dish IDs cannot be null")

    @field:Size(min = 1, message = "Dish IDs list must not be empty")
    val dishIds: List<Long>

)