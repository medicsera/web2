package com.example.spring_jpa.domain.port

import com.example.spring_jpa.domain.model.Dish
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface DishRepositoryPort {
    fun findAll(): List<Dish>
    fun findAllByNamePart(namePart: String): List<Dish>
    fun findById(id: Long): Dish?
    fun findByIdWithRelations(id: Long): Dish?
    fun findByName(name: String): Dish?
    fun findByRestaurantId(restaurantId: Long, pageable: Pageable): Page<Dish>
    fun findByIsAvailableTrue(): List<Dish>
    fun save(dish: Dish): Dish
    fun create(dish: Dish): Dish
    fun update(id: Long, dish: Dish): Dish?
    fun deleteById(id: Long): Boolean
}