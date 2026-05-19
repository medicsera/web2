package com.example.spring_jpa.infrastructure.jpa.adapter

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.infrastructure.jpa.entity.DishEntity
import com.example.spring_jpa.infrastructure.jpa.repository.DishJpaRepository
import com.example.spring_jpa.infrastructure.jpa.repository.RestaurantJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class DishJpaAdapter(
    private val dishJpaRepository: DishJpaRepository,
    private val restaurantJpaRepository: RestaurantJpaRepository
) : DishRepositoryPort {

    override fun findAll(): List<Dish> =
        dishJpaRepository.findAll().map { it.toDomain() }

    override fun findAllByNamePart(namePart: String): List<Dish> =
        dishJpaRepository.findByNameContaining(namePart).map { it.toDomain() }

    override fun findById(id: Long): Dish? =
        dishJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByIdWithRelations(id: Long): Dish? =
        dishJpaRepository.findByIdWithRelations(id)?.toDomain()

    override fun findByName(name: String): Dish? =
        dishJpaRepository.findByName(name)?.toDomain()

    override fun findByRestaurantId(restaurantId: Long, pageable: Pageable): Page<Dish> =
        dishJpaRepository.findByRestaurantId(restaurantId, pageable).map { it.toDomain() }

    override fun findByIsAvailableTrue(): List<Dish> =
        dishJpaRepository.findByIsAvailableTrue().map { it.toDomain()  }

    override fun save(dish: Dish): Dish {
        val restaurant = restaurantJpaRepository.findById(dish.restaurantId).orElse(null)
        val entity = DishEntity(
            id = dish.id.takeIf { it > 0 } ?: 0,
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
            restaurant = restaurant
        )
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun create(dish: Dish): Dish {
        val restaurant = restaurantJpaRepository.findById(dish.restaurantId).orElse(null)
        val entity = DishEntity(
            id = 0,
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
            restaurant = restaurant
        )
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun update(id: Long, dish: Dish): Dish? {
        if (!dishJpaRepository.existsById(id)) return null
        val restaurant = restaurantJpaRepository.findById(dish.restaurantId).orElse(null)
        val entity = DishEntity(
            id = id,
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
            restaurant = restaurant
        )
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun deleteById(id: Long): Boolean {
        if (!dishJpaRepository.existsById(id)) return false
        dishJpaRepository.deleteById(id)
        return true
    }
}
