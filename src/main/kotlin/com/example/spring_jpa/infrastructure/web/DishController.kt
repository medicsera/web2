package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.application.service.DishService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/dishes")
class DishController(
    private val dishService: DishService
) {

    @GetMapping
    fun getAll(
        @RequestParam(required = false) namePart: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<DishResponse>> {
        val dishes = if (namePart != null) {
            dishService.findAllByNamePart(namePart)
        } else {
            dishService.findAll()
        }
        return ResponseEntity.ok(dishes.map { DishResponse.fromDomain(it) })
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<DishResponse> =
        dishService.findById(id)
            ?.let { DishResponse.fromDomain(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @GetMapping("/restaurant/{restaurantId}")
    fun getByRestaurant(
        @PathVariable restaurantId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<DishResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        val dishes = dishService.findByRestaurantId(restaurantId, pageable)
        return ResponseEntity.ok(dishes.map { DishResponse.fromDomain(it) })
    }

    @PostMapping
    fun create(@RequestBody request: CreateDishRequest): ResponseEntity<DishResponse> {
        val dish = dishService.create(
            name = request.name,
            description = request.description,
            price = request.price,
            isAvailable = request.isAvailable,
            restaurantId = request.restaurantId
        )
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(DishResponse.fromDomain(dish))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateDishRequest
    ): ResponseEntity<DishResponse> =
        dishService.update(id, request)
            ?.let { DishResponse.fromDomain(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (dishService.deleteById(id))
            ResponseEntity.noContent().build()
        else
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
}