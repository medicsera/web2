package com.example.spring_jpa.domain.port

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Restaurant

interface RestaurantRepositoryPort {
    fun findAll(): List<Restaurant>
    fun findById(id: Long): Restaurant?
    fun findByName(name: String): Restaurant?
    fun save(restaurant: Restaurant): Restaurant
    fun findDishesByRestaurantId(id: Long): List<Dish>
    fun deleteById(id: Long): Boolean
}