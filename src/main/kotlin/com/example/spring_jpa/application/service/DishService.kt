package com.example.spring_jpa.application.service

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DishService(
    private val dishRepository: DishRepositoryPort
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findAll(): List<Dish> {
        logger.info("Fetching all dishes")
        return dishRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun findAllByNamePart(namePart: String): List<Dish> {
        logger.info("Fetching dishes by name part: {}", namePart)
        return dishRepository.findAllByNamePart(namePart)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Dish? {
        logger.info("Fetching dish with id: {}", id)
        return dishRepository.findById(id)
    }

    @Transactional(readOnly = true)
    fun findByName(name: String): Dish? {
        logger.info("Fetching dish by name: {}", name)
        return dishRepository.findByName(name)
    }

    @Transactional(readOnly = true)
    fun findByRestaurantId(restaurantId: Long, pageable: Pageable): Page<Dish> {
        logger.info("Fetching dishes by restaurant id: {}", restaurantId)
        return dishRepository.findByRestaurantId(restaurantId, pageable)
    }

    @Transactional(readOnly = true)
    fun findAvailable(): List<Dish> {
        logger.info("Fetching available dishes")
        return dishRepository.findByIsAvailableTrue()
    }

    fun create(request: CreateDishRequest): Dish {
        val restaurant = dishRepository.findById(request.restaurantId)
            ?: throw IllegalArgumentException("Restaurant not found: ${request.restaurantId}")

        val dish = Dish(
            name = request.name,
            description = request.description,
            price = request.price,
            isAvailable = request.isAvailable,
            restaurantId = request.restaurantId
        )

        logger.info("Creating dish: {}", dish)
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

        logger.info("Updating dish with id: {}", id)
        return dishRepository.update(id, updated)
    }

    fun deleteById(id: Long): Boolean {
        val deleted = dishRepository.deleteById(id)

        if (deleted) {
            logger.info("Deleted dish with id: {}", id)
        } else {
            logger.warn("Dish with id {} not found", id)
        }

        return deleted
    }
}
