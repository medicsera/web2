package com.example.spring_jpa.infrastructure.jpa.adapter

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.infrastructure.jpa.repository.DishJpaRepository
import com.example.spring_jpa.infrastructure.jpa.entity.DishEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("db")
class DishJpaAdapter(
    private val dishJpaRepository: DishJpaRepository
) : DishRepositoryPort {

    override fun findAll(): List<Dish> =
        dishJpaRepository.findAll().map { it.toDomain() }

    override fun findAllByNamePart(namePart: String): List<Dish> =
        dishJpaRepository.findByNameContaining(namePart).map { it.toDomain() }

    override fun findById(id: Long): Dish? =
        dishJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByName(name: String): Dish? =
        dishJpaRepository.findByName(name).orElse(null)?.toDomain()

    override fun create(dish: Dish): Dish {
        val entity = DishEntity.fromDomain(dish.copy(id = 0))
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun update(id: Long, dish: Dish): Dish? {
        if (!dishJpaRepository.existsById(id)) return null
        val entity = DishEntity.fromDomain(dish.copy(id = id))
        return dishJpaRepository.save(entity).toDomain()
    }

    override fun deleteById(id: Long): Boolean {
        if (!dishJpaRepository.existsById(id)) return false
        dishJpaRepository.deleteById(id)
        return true
    }
}