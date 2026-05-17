package com.example.spring_jpa.application.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal

data class CreateDishRequest(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:Size(max = 255, message = "Description must not exceed 255 characters")
    val description: String? = null,

    @field:NotNull(message = "Price cannot be null")
    @field:Positive(message = "Price must be positive")
    val price: BigDecimal,

    val isAvailable: Boolean? = null,

    val restaurantId: Long? = null
)
