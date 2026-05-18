package com.example.spring_jpa.unit

import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.application.service.UserService
import com.example.spring_jpa.application.dto.CreateUserRequest
import com.example.spring_jpa.application.dto.UpdateUserRequest
import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.UserRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserServiceTest {

    private val repo = mockk<UserRepositoryPort>()
    private val service = UserService(repo)

    private val user = User(id = 1, email = "john@example.com", firstName = "John", lastName = "Doe", isActive = true)

    @Test
    fun `getAll returns list of responses`() {
        every { repo.findAll() } returns listOf(user)

        val result = service.getAll()

        assertEquals(1, result.size)
        assertEquals("john@example.com", result[0].email)
    }

    @Test
    fun `getById returns response when user found`() {
        every { repo.findById(1L) } returns user

        val result = service.getById(1L)

        assertEquals(1L, result.id)
        assertEquals("john@example.com", result.email)
    }

    @Test
    fun `getById throws NotFoundException when user not found`() {
        every { repo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.getById(99L) }
    }

    @Test
    fun `create saves and returns new user`() {
        val request = CreateUserRequest(email = "jane@example.com", firstName = "Jane", lastName = "Smith")
        val newUser = User(id = 2, email = "jane@example.com", firstName = "Jane", lastName = "Smith", isActive = true)
        every { repo.create(any()) } returns newUser

        val result = service.create(request)

        assertEquals(2L, result.id)
        assertEquals("jane@example.com", result.email)
        verify { repo.create(any()) }
    }

    @Test
    fun `update returns updated response when user found`() {
        val request = UpdateUserRequest(email = "john@example.com", firstName = "John", lastName = "Updated")
        val updated = user.copy(lastName = "Updated")
        every { repo.findById(1L) } returns user
        every { repo.update(any()) } returns updated

        val result = service.update(1L, request)

        assertEquals("Updated", result.lastName)
    }

    @Test
    fun `update throws NotFoundException when user not found`() {
        val request = UpdateUserRequest(email = "john@example.com", firstName = "John", lastName = "Doe")
        every { repo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.update(99L, request) }
    }

    @Test
    fun `delete removes user when found`() {
        every { repo.findById(1L) } returns user
        every { repo.deleteById(1L) } returns true

        service.delete(1L)

        verify { repo.deleteById(1L) }
    }

    @Test
    fun `delete throws NotFoundException when user not found`() {
        every { repo.findById(99L) } returns null

        assertThrows<NotFoundException> { service.delete(99L) }
    }

    @Test
    fun `getByEmail returns null when user not found`() {
        every { repo.findByEmail("unknown@example.com") } returns null

        val result = service.getByEmail("unknown@example.com")

        assertNull(result)
    }

    @Test
    fun `getByEmail returns response when user found`() {
        every { repo.findByEmail("john@example.com") } returns user

        val result = service.getByEmail("john@example.com")

        assertNotNull(result)
        assertEquals("john@example.com", result!!.email)
    }
}
