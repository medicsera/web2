package com.example.spring_jpa.domain.port

import com.example.spring_jpa.domain.model.Dish

interface DishRepositoryPort {
    fun findAll(): List<Dish>
    fun findAllByNamePart(namePart: String): List<Dish>
    fun findById(id: Long): Dish?
    fun create(dish: Dish): Dish
    fun update(id: Long, dish: Dish): Dish?
    fun deleteById(id: Long): Boolean
}