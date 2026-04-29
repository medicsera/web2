package com.example.spring_jpa.application.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal


data class CreateDishRequest(

    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    val description: String = "",

    val isAvailable: Boolean = true,

    @field:NotNull(message = "Price cannot be null")

    @field:Positive(message = "Price must be positive")
    val price: BigDecimal,

    @field:NotNull(message = "Restaurant ID cannot be null")
    val restaurantId: Long
)