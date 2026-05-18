package com.example.spring_jpa.unit

import com.example.spring_jpa.application.dto.CreateDishRequest
import com.example.spring_jpa.application.dto.UpdateDishRequest
import com.example.spring_jpa.application.exception.BadRequestException
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.application.service.DishService
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Restaurant
import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.domain.port.RestaurantRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class DishServiceTest {

    private val dishRepo = mockk<DishRepositoryPort>()
    private val restaurantRepo = mockk<RestaurantRepositoryPort>()
    private val service = DishService(dishRepo, restaurantRepo)

    private val restaurant = Restaurant(id = 1, name = "Sushi Place", address = "123 Main St")
    private val dish = Dish(id = 1, name = "Roll", description = "Classic roll", price = BigDecimal("9.99"), isAvailable = true, restaurantId = 1)

    @Test
    fun `findAll returns all dishes`() {
        every { dishRepo.findAll() } returns listOf(dish)

        val result = service.findAll()

        assertEquals(1, result.size)
        assertEquals("Roll", result[0].name)
    }

    @Test
    fun `findById returns dish when found`() {
        every { dishRepo.findById(1L) } returns dish

        val result = service.findById(1L)

        assertEquals(1L, result.id)
        assertEquals("Roll", result.name)
    }

    @Test
    fun `findById throws NotFoundException when not found`() {
        every { dishRepo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.findById(99L) }
    }

    @Test
    fun `create saves dish when restaurant exists`() {
        val request = CreateDishRequest(name = "New Roll", price = BigDecimal("12.50"), restaurantId = 1)
        val created = Dish(id = 2, name = "New Roll", description = "", price = BigDecimal("12.50"), isAvailable = true, restaurantId = 1)
        every { restaurantRepo.findById(1L) } returns restaurant
        every { dishRepo.create(any()) } returns created

        val result = service.create(request)

        assertEquals(2L, result.id)
        assertEquals("New Roll", result.name)
        verify { dishRepo.create(any()) }
    }

    @Test
    fun `create throws BadRequestException when restaurantId is null`() {
        val request = CreateDishRequest(name = "Roll", price = BigDecimal("9.99"), restaurantId = null)

        assertThrows<BadRequestException> { service.create(request) }
    }

    @Test
    fun `create throws NotFoundException when restaurant not found`() {
        val request = CreateDishRequest(name = "Roll", price = BigDecimal("9.99"), restaurantId = 99)
        every { restaurantRepo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.create(request) }
    }

    @Test
    fun `update returns updated dish when found`() {
        val request = UpdateDishRequest(name = "Updated Roll", price = BigDecimal("14.00"))
        val updated = dish.copy(name = "Updated Roll", price = BigDecimal("14.00"))
        every { dishRepo.findById(1L) } returns dish
        every { dishRepo.update(1L, any()) } returns updated

        val result = service.update(1L, request)

        assertEquals("Updated Roll", result.name)
        assertEquals(BigDecimal("14.00"), result.price)
    }

    @Test
    fun `update throws NotFoundException when dish not found`() {
        val request = UpdateDishRequest(name = "Updated", price = BigDecimal("10.00"))
        every { dishRepo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.update(99L, request) }
    }

    @Test
    fun `deleteById deletes dish when found`() {
        every { dishRepo.deleteById(1L) } returns true

        service.deleteById(1L)

        verify { dishRepo.deleteById(1L) }
    }

    @Test
    fun `deleteById throws NotFoundException when dish not found`() {
        every { dishRepo.deleteById(99L) } returns false

        assertThrows<NotFoundException> { service.deleteById(99L) }
    }
}
