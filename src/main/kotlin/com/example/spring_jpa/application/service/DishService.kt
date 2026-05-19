package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.CreateDishRequest
import com.example.spring_jpa.application.dto.UpdateDishRequest
import com.example.spring_jpa.application.exception.BadRequestException
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.domain.port.RestaurantRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DishService(
    private val dishRepository: DishRepositoryPort,
    private val restaurantRepository: RestaurantRepositoryPort
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
    @Cacheable(cacheNames = ["dishes"], key = "'id::' + #id")
    fun findById(id: Long): Dish {
        logger.info("Loading dish id={} from DB", id)
        return dishRepository.findById(id)
            ?: run {
                logger.warn("Dish not found with id: {}", id)
                throw NotFoundException("Dish not found with id: $id")
            }
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

    @Caching(evict = [
        CacheEvict(cacheNames = ["dishes"], allEntries = true),
        CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    ])
    fun create(request: CreateDishRequest): Dish {
        val restId = request.restaurantId
            ?: throw BadRequestException("Restaurant ID cannot be null")
        restaurantRepository.findById(restId)
            ?: run {
                logger.warn("Restaurant not found with id: {}", restId)
                throw NotFoundException("Restaurant not found with id: $restId")
            }
        val dish = Dish(
            name = request.name,
            description = request.description ?: "",
            price = request.price,
            isAvailable = request.isAvailable ?: true,
            restaurantId = restId
        )
        val created = dishRepository.create(dish)
        logger.info("Created dish id={}, dishes and restaurants caches evicted", created.id)
        return created
    }

    @Caching(evict = [
        CacheEvict(cacheNames = ["dishes"], allEntries = true),
        CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    ])
    fun update(id: Long, request: UpdateDishRequest): Dish {
        val existing = dishRepository.findById(id)
            ?: run {
                logger.warn("Dish not found with id: {}", id)
                throw NotFoundException("Dish not found with id: $id")
            }
        val updated = existing.copy(
            name = request.name,
            description = request.description ?: "",
            price = request.price,
            isAvailable = request.isAvailable ?: existing.isAvailable
        )
        val saved = dishRepository.update(id, updated)
            ?: throw NotFoundException("Dish not found with id: $id")
        logger.info("Updated dish id={}, dishes and restaurants caches evicted", id)
        return saved
    }

    @Caching(evict = [
        CacheEvict(cacheNames = ["dishes"], allEntries = true),
        CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    ])
    fun deleteById(id: Long) {
        val deleted = dishRepository.deleteById(id)
        if (!deleted) {
            logger.warn("Dish not found with id: {}", id)
            throw NotFoundException("Dish not found with id: $id")
        }
        logger.info("Deleted dish id={}, dishes and restaurants caches evicted", id)
    }
}
