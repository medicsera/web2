package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.CreateDishRequest
import com.example.spring_jpa.application.dto.DishResponse
import com.example.spring_jpa.application.dto.UpdateDishRequest
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.port.DishRepositoryPort
import org.springframework.stereotype.Service

@Service
class DishService(
    private val dishRepository: DishRepositoryPort
) {

    fun getAll(namePart: String?): List<DishResponse> {
        val dishes = if (namePart.isNullOrBlank()) {
            dishRepository.findAll()
        } else {
            dishRepository.findAllByNamePart(namePart)
        }
        return dishes.map { DishResponse.fromDomain(it) }
    }

    fun getById(id: Long): DishResponse? =
        dishRepository.findById(id)?.let { DishResponse.fromDomain(it) }

    fun create(request: CreateDishRequest): DishResponse {
        val dish = Dish(
            name = request.name,
            description = request.description,
            price = request.price,
            isAvailable = request.isAvailable
        )
        return DishResponse.fromDomain(dishRepository.create(dish))
    }

    fun update(id: Long, request: UpdateDishRequest): DishResponse? {
        val dish = Dish(
            id = id,
            name = request.name,
            description = request.description,
            price = request.price,
            isAvailable = request.isAvailable
        )
        return dishRepository.update(id, dish)?.let { DishResponse.fromDomain(it) }
    }

    fun delete(id: Long): Boolean = dishRepository.deleteById(id)
}