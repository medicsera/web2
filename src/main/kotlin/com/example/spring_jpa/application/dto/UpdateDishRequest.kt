package com.example.spring_jpa.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class UpdateDishRequest(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    val description: String? = null,

    @field:NotNull(message = "Price cannot be null")
    @field:Positive(message = "Price must be positive")
    val price: BigDecimal,

    val isAvailable: Boolean? = null
)
