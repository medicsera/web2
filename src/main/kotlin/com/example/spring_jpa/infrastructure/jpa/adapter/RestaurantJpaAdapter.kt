package com.example.spring_jpa.infrastructure.jpa.adapter

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Restaurant
import com.example.spring_jpa.domain.port.RestaurantRepositoryPort
import com.example.spring_jpa.infrastructure.jpa.entity.RestaurantEntity
import com.example.spring_jpa.infrastructure.jpa.repository.RestaurantJpaRepository
import org.springframework.stereotype.Repository

@Repository
class RestaurantJpaAdapter(
    private val jpaRepository: RestaurantJpaRepository
) : RestaurantRepositoryPort {

    override fun findAll(): List<Restaurant> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: Long): Restaurant? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun save(restaurant: Restaurant): Restaurant {
        val entity = jpaRepository.save(restaurant.toEntity())
        return entity.toDomain()
    }

    override fun findDishesByRestaurantId(id: Long): List<Dish> {
        // Реализация через DishJpaRepository
        TODO("Реализовать позже")
    }

    override fun deleteById(id: Long): Boolean {
        if (!jpaRepository.existsById(id)) return false
        jpaRepository.deleteById(id)
        return true
    }

    private fun RestaurantEntity.toDomain() = Restaurant(
        id = id ?: 0L,
        name = name,
        address = address
    )

    private fun Restaurant.toEntity() = RestaurantEntity(
        id = id,
        name = name,
        address = address
    )
}