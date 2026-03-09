package com.example.spring_jpa.infrastructure.jpa.adapter

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.infrastructure.jpa.entity.DishEntity
import com.example.spring_jpa.infrastructure.jpa.repository.DishJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class DishJpaAdapter(
    private val dishJpaRepository: DishJpaRepository
) : DishRepositoryPort {

    override fun findAll(): List<Dish> =
        dishJpaRepository.findAll().map { it.toDomain() }

    override fun findAllByNamePart(namePart: String): List<Dish> =
        dishJpaRepository.findByNameContaining(namePart).map { it.toDomain() }

    override fun findById(id: Long): Dish? =
        dishJpaRepository.findById(id).orElse(null)?.toDomain()  // ← orElse(null)

    override fun findByIdWithRelations(id: Long): Dish? =
        dishJpaRepository.findByIdWithRelations(id)?.toDomain()

    override fun findByName(name: String): Dish? =
        dishJpaRepository.findByName(name)?.toDomain()

    override fun findByRestaurantId(restaurantId: Long, pageable: Pageable): Page<Dish> =
        dishJpaRepository.findByRestaurantId(restaurantId, pageable).map { it.toDomain() }

    override fun findByIsAvailableTrue(): List<Dish> =
        dishJpaRepository.findByIsAvailableTrue().map { it.toDomain() }

    override fun save(dish: Dish): Dish {
        val entity = DishEntity.fromDomain(dish)  // ← Используем fromDomain из DishEntity
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun create(dish: Dish): Dish {
        val entity = DishEntity.fromDomain(dish.copy(id = 0))  // ← fromDomain
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun update(id: Long, dish: Dish): Dish? {
        if (!dishJpaRepository.existsById(id)) return null
        val entity = DishEntity.fromDomain(dish.copy(id = id))  // ← fromDomain
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun deleteById(id: Long): Boolean {
        if (!dishJpaRepository.existsById(id)) return false
        dishJpaRepository.deleteById(id)
        return true
    }
}