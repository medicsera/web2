package com.example.spring_jpa.domain.port

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Restaurant

interface RestaurantRepositoryPort {
    fun findAll(): List<Restaurant>  // ← Было: List
    fun findById(id: Long): Restaurant?
    fun save(restaurant: Restaurant): Restaurant
    fun findDishesByRestaurantId(id: Long): List<Dish>  // ← Было: List
    fun deleteById(id: Long): Boolean  // ← Добавить
}