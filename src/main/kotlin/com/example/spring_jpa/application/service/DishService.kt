package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.CreateDishRequest
import com.example.spring_jpa.application.dto.UpdateDishRequest
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.domain.port.RestaurantRepositoryPort
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class DishService(
    private val dishRepository: DishRepositoryPort,
    private val restaurantRepository: RestaurantRepositoryPort
) {
    @Transactional(readOnly = true)
    fun findAll(): List<Dish> = dishRepository.findAll()

    @Transactional(readOnly = true)
    fun findAllByNamePart(namePart: String): List<Dish> =
        dishRepository.findAllByNamePart(namePart)

    @Transactional(readOnly = true)
    fun findById(id: Long): Dish? = dishRepository.findByIdWithRelations(id)

    @Transactional(readOnly = true)
    fun findByName(name: String): Dish? = dishRepository.findByName(name)

    @Transactional(readOnly = true)
    fun findByRestaurantId(restaurantId: Long, pageable: Pageable): Page<Dish> =
        dishRepository.findByRestaurantId(restaurantId, pageable)

    @Transactional(readOnly = true)
    fun findAvailable(): List<Dish> = dishRepository.findByIsAvailableTrue()

    fun create(
        name: String,
        description: String,
        price: BigDecimal,
        isAvailable: Boolean,
        restaurantId: Long
    ): Dish {
        restaurantRepository.findById(restaurantId)
            ?: throw IllegalArgumentException("Restaurant not found: $restaurantId")

        val dish = Dish(
            name = name,
            description = description,
            price = price,
            isAvailable = isAvailable,
            restaurantId = restaurantId
        )
        return dishRepository.create(dish)
    }

    fun update(id: Long, request: UpdateDishRequest): Dish? {
        val existing = dishRepository.findById(id) ?: return null
        val updated = existing.copy(
            name = request.name,
            description = request.description,
            price = request.price,
            isAvailable = request.isAvailable
        )
        return dishRepository.update(id, updated)
    }

    fun deleteById(id: Long): Boolean = dishRepository.deleteById(id)
}