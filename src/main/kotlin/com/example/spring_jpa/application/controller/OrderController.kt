package com.example.spring_jpa.application.controller

import com.example.spring_jpa.application.dto.CreateOrderRequest
import com.example.spring_jpa.application.service.OrderService
import com.example.spring_jpa.domain.model.Order
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/orders")

class OrderController(private val orderService: OrderService) {
    @PostMapping

    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<Order> {
        return ResponseEntity.ok(orderService.create(request))
    }

}