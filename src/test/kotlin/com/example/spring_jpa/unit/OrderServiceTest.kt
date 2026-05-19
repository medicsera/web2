package com.example.spring_jpa.unit

import com.example.spring_jpa.application.dto.CreateOrderRequest
import com.example.spring_jpa.application.dto.UpdateOrderStatusRequest
import com.example.spring_jpa.application.exception.BadRequestException
import com.example.spring_jpa.application.exception.InvalidOrderStateException
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.application.service.OrderService
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Order
import com.example.spring_jpa.domain.model.OrderStatus
import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.domain.port.OrderRepositoryPort
import com.example.spring_jpa.domain.port.UserRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderServiceTest {

    private val orderRepo = mockk<OrderRepositoryPort>()
    private val userRepo = mockk<UserRepositoryPort>()
    private val dishRepo = mockk<DishRepositoryPort>()
    private val service = OrderService(orderRepo, userRepo, dishRepo)

    private val user = User(id = 1, email = "john@example.com", firstName = "John", lastName = "Doe")
    private val dish = Dish(id = 1, name = "Roll", description = "Classic", price = BigDecimal("9.99"), isAvailable = true, restaurantId = 1)
    private val order = Order(id = 1, status = OrderStatus.PENDING, createdAt = LocalDateTime.now(), userId = 1, dishIds = listOf(1), dishes = listOf(dish))

    @Test
    fun `findById returns order when found`() {
        every { orderRepo.findByIdWithRelations(1L) } returns order

        val result = service.findById(1L)

        assertEquals(1L, result.id)
        assertEquals(OrderStatus.PENDING, result.status)
    }

    @Test
    fun `findById throws NotFoundException when not found`() {
        every { orderRepo.findByIdWithRelations(99L) } returns null

        assertThrows<NotFoundException> { service.findById(99L) }
    }

    @Test
    fun `create saves order when user and dishes exist`() {
        val request = CreateOrderRequest(userId = 1, dishIds = listOf(1))
        val created = order.copy(id = 2)
        every { userRepo.findById(1L) } returns user
        every { dishRepo.findById(1L) } returns dish
        every { orderRepo.create(any()) } returns created

        val result = service.create(request)

        assertEquals(2L, result.id)
        verify { orderRepo.create(any()) }
    }

    @Test
    fun `create throws BadRequestException when user not found`() {
        val request = CreateOrderRequest(userId = 99, dishIds = listOf(1))
        every { userRepo.findById(99L) } returns null

        assertThrows<BadRequestException> { service.create(request) }
    }

    @Test
    fun `create throws BadRequestException when dish not found`() {
        val request = CreateOrderRequest(userId = 1, dishIds = listOf(99))
        every { userRepo.findById(1L) } returns user
        every { dishRepo.findById(99L) } returns null

        assertThrows<BadRequestException> { service.create(request) }
    }

    @Test
    fun `updateStatus transitions PENDING to CONFIRMED`() {
        val request = UpdateOrderStatusRequest(status = OrderStatus.CONFIRMED)
        val confirmed = order.copy(status = OrderStatus.CONFIRMED)
        every { orderRepo.findById(1L) } returns order
        every { orderRepo.updateStatus(1L, OrderStatus.CONFIRMED) } returns confirmed

        val result = service.updateStatus(1L, request)

        assertEquals(OrderStatus.CONFIRMED, result.status)
    }

    @Test
    fun `updateStatus throws InvalidOrderStateException for invalid transition`() {
        val request = UpdateOrderStatusRequest(status = OrderStatus.DELIVERED)
        every { orderRepo.findById(1L) } returns order

        assertThrows<InvalidOrderStateException> { service.updateStatus(1L, request) }
    }

    @Test
    fun `updateStatus throws NotFoundException when order not found`() {
        val request = UpdateOrderStatusRequest(status = OrderStatus.CONFIRMED)
        every { orderRepo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.updateStatus(99L, request) }
    }

    @Test
    fun `deleteById deletes order when found`() {
        every { orderRepo.deleteById(1L) } returns true

        service.deleteById(1L)

        verify { orderRepo.deleteById(1L) }
    }

    @Test
    fun `deleteById throws NotFoundException when order not found`() {
        every { orderRepo.deleteById(99L) } returns false

        assertThrows<NotFoundException> { service.deleteById(99L) }
    }
}
