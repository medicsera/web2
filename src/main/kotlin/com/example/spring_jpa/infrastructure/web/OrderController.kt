package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.application.service.OrderService
import com.example.spring_jpa.domain.model.OrderStatus
import jakarta.validation.Valid
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
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) status: OrderStatus?
    ): ResponseEntity<List<OrderResponse>> =
        ResponseEntity.ok(orderService.findAll(userId, status).map { OrderResponse.fromDomain(it) })

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(OrderResponse.fromDomain(orderService.findById(id)))

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
    fun create(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.fromDomain(order))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(OrderResponse.fromDomain(orderService.updateStatus(id, request)))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        orderService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
