package com.example.spring_jpa.application.dto

import com.example.spring_jpa.domain.model.Dish
import java.math.BigDecimal

data class CreateDishRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val isAvailable: Boolean = true,
    val restaurantId: Long
)

data class UpdateDishRequest(
    val name: String,
    val description: String,
    val price: BigDecimal,
    val isAvailable: Boolean = true
)

data class DishResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val isAvailable: Boolean,
    val restaurantId: Long
) {
    companion object {
        fun fromDomain(dish: Dish): DishResponse = DishResponse(
            id = dish.id,
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
            restaurantId = dish.restaurantId
        )
    }
}