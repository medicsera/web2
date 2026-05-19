package com.example.spring_jpa.application.dto

import com.example.spring_jpa.domain.model.Restaurant

data class RestaurantResponse(
    val id: Long,
    val name: String,
    val address: String
) {
    companion object {
        fun fromDomain(restaurant: Restaurant): RestaurantResponse = RestaurantResponse(
            id = restaurant.id,
            name = restaurant.name,
            address = restaurant.address
        )
    }
}
