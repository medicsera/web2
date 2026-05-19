package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.application.exception.BadRequestException
import com.example.spring_jpa.application.service.OrderService
import com.example.spring_jpa.domain.model.OrderStatus
import com.example.spring_jpa.domain.port.UserRepositoryPort
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
    private val userRepository: UserRepositoryPort
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAll(
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) status: OrderStatus?
    ): ResponseEntity<List<OrderResponse>> =
        ResponseEntity.ok(orderService.findAll(userId, status).map { OrderResponse.fromDomain(it) })

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<OrderResponse> {
        val order = orderService.findById(id)
        val isAdmin = principal.authorities.any { it.authority == "ROLE_ADMIN" }
        if (!isAdmin) {
            val user = userRepository.findByEmail(principal.username)
            if (user == null || order.userId != user.id) {
                throw AccessDeniedException("Access denied")
            }
        }
        return ResponseEntity.ok(OrderResponse.fromDomain(order))
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId, authentication.name)")
    fun getByUser(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return ResponseEntity.ok(orderService.findByUserId(userId, pageable).map { OrderResponse.fromDomain(it) })
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getByStatus(
        @PathVariable status: OrderStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return ResponseEntity.ok(orderService.findByStatus(status, pageable).map { OrderResponse.fromDomain(it) })
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    fun create(
        @Valid @RequestBody request: CreateOrderRequest,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<OrderResponse> {
        val user = userRepository.findByEmail(principal.username)
            ?: throw BadRequestException("Authenticated user not found")
        val order = orderService.create(request.copy(userId = user.id))
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.fromDomain(order))
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(OrderResponse.fromDomain(orderService.updateStatus(id, request)))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        orderService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
