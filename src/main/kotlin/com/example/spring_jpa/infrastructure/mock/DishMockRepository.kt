package com.example.spring_jpa.infrastructure.mock

import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("mock")
class DishMockRepository : DishRepositoryPort {
    private val storage = mutableMapOf<Long, Dish>()
    private var sequence = 1L

    override fun findAll(): List<Dish> = storage.values.toList()

    override fun findAllByNamePart(namePart: String): List<Dish> =
        storage.values.filter {
            it.name.contains(namePart, ignoreCase = true)
        }

    override fun findById(id: Long): Dish? = storage[id]

    override fun create(dish: Dish): Dish {
        val saved = dish.copy(id = sequence++)
        storage[saved.id] = saved
        return saved
    }

    override fun update(id: Long, dish: Dish): Dish? {
        if (!storage.containsKey(id)) return null

        val updated = dish.copy(id = id)
        storage[id] = updated
        return updated
    }

    override fun deleteById(id: Long): Boolean = storage.remove(id) != null


}