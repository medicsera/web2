package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Restaurant
import com.example.spring_jpa.domain.port.RestaurantRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RestaurantService(
    private val restaurantRepository: RestaurantRepositoryPort
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): Restaurant? =
        restaurantRepository.findById(id)

    @Transactional(readOnly = true)
    fun findAll(): List<Restaurant> =
        restaurantRepository.findAll()

    fun create(request: CreateRestaurantRequest): Restaurant {
        val restaurant = Restaurant(
            name = request.name,
            address = request.address
        )
        return restaurantRepository.save(restaurant)
    }

    fun update(id: Long, request: CreateRestaurantRequest): Restaurant? {
        val existing = restaurantRepository.findById(id) ?: return null
        val updated = existing.copy(
            name = request.name,
            address = request.address
        )
        return restaurantRepository.save(updated)
    }

    fun deleteById(id: Long): Boolean {
        // Нужно добавить метод в порт
        TODO("Добавить deleteById в RestaurantRepositoryPort")
    }

    fun findDishesByRestaurantId(id: Long): List<Dish> =
        restaurantRepository.findDishesByRestaurantId(id)
}