package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.exception.AlreadyExistsException
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Restaurant
import com.example.spring_jpa.domain.port.RestaurantRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RestaurantService(
    private val restaurantRepository: RestaurantRepositoryPort
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ["restaurants"], key = "#id")
    fun findById(id: Long): Restaurant {
        logger.info("Loading restaurant id={} from DB", id)
        return restaurantRepository.findById(id)
            ?: run {
                logger.warn("Restaurant not found with id: {}", id)
                throw NotFoundException("Restaurant not found with id: $id")
            }
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ["restaurants"])
    fun findAll(): List<Restaurant> {
        logger.info("Loading all restaurants from DB")
        return restaurantRepository.findAll()
    }

    @CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    fun create(request: CreateRestaurantRequest): Restaurant {
        if (restaurantRepository.findByName(request.name) != null) {
            logger.warn("Restaurant already exists with name: {}", request.name)
            throw AlreadyExistsException("Restaurant already exists with name: ${request.name}")
        }
        val restaurant = Restaurant(name = request.name, address = request.address)
        val created = restaurantRepository.save(restaurant)
        logger.info("Created restaurant id={}, restaurants cache evicted", created.id)
        return created
    }

    @CacheEvict(cacheNames = ["restaurants"], allEntries = true)
    fun update(id: Long, request: CreateRestaurantRequest): Restaurant {
        restaurantRepository.findById(id)
            ?: run {
                logger.warn("Restaurant not found with id: {}", id)
                throw NotFoundException("Restaurant not found with id: $id")
            }
        val existing = restaurantRepository.findByName(request.name)
        if (existing != null && existing.id != id) {
            throw AlreadyExistsException("Restaurant already exists with name: ${request.name}")
        }
        val updated = Restaurant(id = id, name = request.name, address = request.address)
        val saved = restaurantRepository.save(updated)
        logger.info("Updated restaurant id={}, restaurants cache evicted", id)
        return saved
    }

    @Caching(evict = [
        CacheEvict(cacheNames = ["restaurants"], allEntries = true),
        CacheEvict(cacheNames = ["dishes"], allEntries = true)
    ])
    fun deleteById(id: Long) {
        restaurantRepository.findById(id)
            ?: run {
                logger.warn("Restaurant not found with id: {}", id)
                throw NotFoundException("Restaurant not found with id: $id")
            }
        restaurantRepository.deleteById(id)
        logger.info("Deleted restaurant id={}, restaurants and dishes caches evicted", id)
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ["dishes"], key = "#id")
    fun findDishesByRestaurantId(id: Long): List<Dish> {
        restaurantRepository.findById(id)
            ?: throw NotFoundException("Restaurant not found with id: $id")
        logger.info("Loading dishes for restaurant id={} from DB", id)
        return restaurantRepository.findDishesByRestaurantId(id)
    }
}
