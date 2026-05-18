package com.example.spring_jpa.unit

import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.exception.AlreadyExistsException
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.application.service.RestaurantService
import com.example.spring_jpa.domain.model.Dish
import com.example.spring_jpa.domain.model.Restaurant
import com.example.spring_jpa.domain.port.RestaurantRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class RestaurantServiceTest {

    private val repo = mockk<RestaurantRepositoryPort>()
    private val service = RestaurantService(repo)

    private val restaurant = Restaurant(id = 1, name = "Sushi Place", address = "123 Main St")

    @Test
    fun `findAll returns all restaurants`() {
        every { repo.findAll() } returns listOf(restaurant)

        val result = service.findAll()

        assertEquals(1, result.size)
        assertEquals("Sushi Place", result[0].name)
    }

    @Test
    fun `findById returns restaurant when found`() {
        every { repo.findById(1L) } returns restaurant

        val result = service.findById(1L)

        assertEquals(1L, result.id)
        assertEquals("Sushi Place", result.name)
    }

    @Test
    fun `findById throws NotFoundException when not found`() {
        every { repo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.findById(99L) }
    }

    @Test
    fun `create saves restaurant when name is unique`() {
        val request = CreateRestaurantRequest(name = "New Place", address = "456 Oak Ave")
        val saved = Restaurant(id = 2, name = "New Place", address = "456 Oak Ave")
        every { repo.findByName("New Place") } returns null
        every { repo.save(any()) } returns saved

        val result = service.create(request)

        assertEquals(2L, result.id)
        assertEquals("New Place", result.name)
        verify { repo.save(any()) }
    }

    @Test
    fun `create throws AlreadyExistsException when name is taken`() {
        val request = CreateRestaurantRequest(name = "Sushi Place", address = "Different Address")
        every { repo.findByName("Sushi Place") } returns restaurant

        assertThrows<AlreadyExistsException> { service.create(request) }
    }

    @Test
    fun `update saves updated restaurant when name is not taken`() {
        val request = CreateRestaurantRequest(name = "Updated Name", address = "New Address")
        val updated = restaurant.copy(name = "Updated Name", address = "New Address")
        every { repo.findById(1L) } returns restaurant
        every { repo.findByName("Updated Name") } returns null
        every { repo.save(any()) } returns updated

        val result = service.update(1L, request)

        assertEquals("Updated Name", result.name)
    }

    @Test
    fun `update throws NotFoundException when restaurant not found`() {
        val request = CreateRestaurantRequest(name = "Updated", address = "Addr")
        every { repo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.update(99L, request) }
    }

    @Test
    fun `update throws AlreadyExistsException when new name belongs to another restaurant`() {
        val other = Restaurant(id = 2, name = "Burger Joint", address = "789 Pine Rd")
        val request = CreateRestaurantRequest(name = "Burger Joint", address = "New Address")
        every { repo.findById(1L) } returns restaurant
        every { repo.findByName("Burger Joint") } returns other

        assertThrows<AlreadyExistsException> { service.update(1L, request) }
    }

    @Test
    fun `deleteById deletes restaurant when found`() {
        every { repo.findById(1L) } returns restaurant
        every { repo.deleteById(1L) } returns true

        service.deleteById(1L)

        verify { repo.deleteById(1L) }
    }

    @Test
    fun `deleteById throws NotFoundException when not found`() {
        every { repo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.deleteById(99L) }
    }

    @Test
    fun `findDishesByRestaurantId returns dishes when restaurant exists`() {
        val dish = Dish(id = 1, name = "Roll", description = "Yummy", price = BigDecimal("9.99"), isAvailable = true, restaurantId = 1)
        every { repo.findById(1L) } returns restaurant
        every { repo.findDishesByRestaurantId(1L) } returns listOf(dish)

        val result = service.findDishesByRestaurantId(1L)

        assertEquals(1, result.size)
        assertEquals("Roll", result[0].name)
    }

    @Test
    fun `findDishesByRestaurantId throws NotFoundException when restaurant not found`() {
        every { repo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.findDishesByRestaurantId(99L) }
    }
}
