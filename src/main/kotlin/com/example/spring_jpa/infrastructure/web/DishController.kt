package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.application.service.DishService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/dishes")
class DishController (
    private val dishService: DishService
) {
    @GetMapping
    fun getAll(
        @RequestParam(required = false) namePart: String?
    ): ResponseEntity<List<DishResponse>> =
        ResponseEntity.ok(dishService.getAll(namePart))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Any> =
        dishService.getById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf(
                    "status" to "NOT_FOUND",
                    "error" to "Not Found",
                    "message" to "Dish with id $id not found"
                ))

    @PostMapping
    fun create(@RequestBody request: CreateDishRequest): ResponseEntity<Any> {
        // Проверяем, существует ли блюдо с таким именем
        val existingDish = dishService.getByName(request.name)

        return if (existingDish != null) {
            // Если существует — возвращаем 200 OK
            ResponseEntity.ok(existingDish)
        } else {
            // Если новое — создаём и возвращаем 201 Created
            ResponseEntity.status(HttpStatus.CREATED)
                .body(dishService.create(request))
        }
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateDishRequest
    ): ResponseEntity<Any> =
        dishService.update(id, request)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf(
                    "status" to "NOT_FOUND",
                    "error" to "Not Found",
                    "message" to "Dish with id $id not found"
                ))



    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> =
        if (dishService.delete(id))
            ResponseEntity.noContent().build()
        else
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf(
                    "status" to "NOT_FOUND",
                    "error" to "Not Found",
                    "message" to "Dish with id $id not found"
                ))
}