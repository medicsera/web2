package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.application.service.DishService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
        val dishes = if (namePart != null) dishService.findAllByNamePart(namePart) else dishService.findAll()
        return ResponseEntity.ok(dishes.map { DishResponse.fromDomain(it) })
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<DishResponse> =
        ResponseEntity.ok(DishResponse.fromDomain(dishService.findById(id)))

    @GetMapping("/restaurant/{restaurantId}")
    fun getByRestaurant(
        @PathVariable restaurantId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<DishResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("name").ascending())
        return ResponseEntity.ok(dishService.findByRestaurantId(restaurantId, pageable).map { DishResponse.fromDomain(it) })
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: CreateDishRequest): ResponseEntity<DishResponse> {
        val dish = dishService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(DishResponse.fromDomain(dish))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateDishRequest
    ): ResponseEntity<DishResponse> =
        ResponseEntity.ok(DishResponse.fromDomain(dishService.update(id, request)))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        dishService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
