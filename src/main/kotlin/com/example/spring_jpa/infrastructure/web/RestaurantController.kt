package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.dto.DishResponse
import com.example.spring_jpa.application.dto.RestaurantResponse
import com.example.spring_jpa.application.service.RestaurantService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService
) {

    @GetMapping
    fun getAll(): ResponseEntity<List<RestaurantResponse>> {
        val restaurants = restaurantService.findAll()
        return ResponseEntity.ok(restaurants.map { RestaurantResponse.fromDomain(it) })
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<RestaurantResponse> =
        restaurantService.findById(id)
            ?.let { RestaurantResponse.fromDomain(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @PostMapping
    fun create(@RequestBody request: CreateRestaurantRequest): ResponseEntity<RestaurantResponse> {
        val restaurant = restaurantService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(RestaurantResponse.fromDomain(restaurant))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: CreateRestaurantRequest
    ): ResponseEntity<RestaurantResponse> =
        restaurantService.update(id, request)
            ?.let { RestaurantResponse.fromDomain(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (restaurantService.deleteById(id))
            ResponseEntity.noContent().build()
        else
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @GetMapping("/{id}/dishes")
    fun getDishesByRestaurant(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<DishResponse>> {
        val dishes = restaurantService.findDishesByRestaurantId(id)
        return ResponseEntity.ok(dishes.map { DishResponse.fromDomain(it) })
    }
}