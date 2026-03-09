package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.application.service.OrderService
import com.example.spring_jpa.domain.model.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<OrderResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return ResponseEntity.ok(emptyList()) // TODO: реализовать
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<OrderResponse> =
        orderService.findById(id)
            ?.let { OrderResponse.fromDomain(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @GetMapping("/user/{userId}")
    fun getByUser(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val orders = orderService.findByUserId(userId, pageable)
        return ResponseEntity.ok(orders.map { OrderResponse.fromDomain(it) })
    }

    @GetMapping("/status/{status}")
    fun getByStatus(
        @PathVariable status: OrderStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val orders = orderService.findByStatus(status, pageable)
        return ResponseEntity.ok(orders.map { OrderResponse.fromDomain(it) })
    }

    @PostMapping
    fun create(@RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(OrderResponse.fromDomain(order))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> =
        orderService.updateStatus(id, request)
            ?.let { OrderResponse.fromDomain(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (orderService.deleteById(id))
            ResponseEntity.noContent().build()
        else
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
}