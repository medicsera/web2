package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.CreateDishRequest
import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.dto.DishResponse
import com.example.spring_jpa.application.dto.RestaurantResponse
import com.example.spring_jpa.application.service.DishService
import com.example.spring_jpa.application.service.RestaurantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService,
    private val dishService: DishService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<RestaurantResponse>> =
        ResponseEntity.ok(restaurantService.findAll().map { RestaurantResponse.fromDomain(it) })

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<RestaurantResponse> =
        ResponseEntity.ok(RestaurantResponse.fromDomain(restaurantService.findById(id)))

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: CreateRestaurantRequest): ResponseEntity<RestaurantResponse> {
        val restaurant = restaurantService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(RestaurantResponse.fromDomain(restaurant))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateRestaurantRequest
    ): ResponseEntity<RestaurantResponse> =
        ResponseEntity.ok(RestaurantResponse.fromDomain(restaurantService.update(id, request)))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        restaurantService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/dishes")
    fun getDishesByRestaurant(@PathVariable id: Long): ResponseEntity<List<DishResponse>> =
        ResponseEntity.ok(restaurantService.findDishesByRestaurantId(id).map { DishResponse.fromDomain(it) })

    @PostMapping("/{id}/dishes")
    @PreAuthorize("hasRole('ADMIN')")
    fun createDish(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateDishRequest
    ): ResponseEntity<DishResponse> {
        val dish = dishService.create(request.copy(restaurantId = id))
        return ResponseEntity.status(HttpStatus.CREATED).body(DishResponse.fromDomain(dish))
    }
}
