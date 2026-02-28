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
    fun getById(@PathVariable id: Long): ResponseEntity<DishResponse> =
        dishService.getById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    fun create(@RequestBody request: CreateDishRequest): ResponseEntity<DishResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(dishService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateDishRequest
    ): ResponseEntity<DishResponse> =
        dishService.update(id, request)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (dishService.delete(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
}