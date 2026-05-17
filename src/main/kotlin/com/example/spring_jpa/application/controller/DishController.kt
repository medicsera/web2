package com.example.spring_jpa.application.controller

import com.example.spring_jpa.application.dto.CreateDishRequest
import com.example.spring_jpa.application.service.DishService
import com.example.spring_jpa.domain.model.Dish
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController("legacyDishController")
@RequestMapping("/dishes")

class DishController(private val dishService: DishService) {
    @PostMapping

    fun createDish(@Valid @RequestBody request: CreateDishRequest): ResponseEntity<Dish> {
        return ResponseEntity.ok(dishService.create(request))
    }
}