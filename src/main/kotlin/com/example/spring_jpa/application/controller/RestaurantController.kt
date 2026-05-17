package com.example.spring_jpa.application.controller

import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.service.RestaurantService
import com.example.spring_jpa.domain.model.Restaurant
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController("legacyRestaurantController")
@RequestMapping("/restaurants")
class RestaurantController(private val restaurantService: RestaurantService) {
    @PostMapping

    fun createRestaurant(@Valid @RequestBody request: CreateRestaurantRequest): ResponseEntity<Restaurant> {
        return ResponseEntity.ok(restaurantService.create(request))
    }
}