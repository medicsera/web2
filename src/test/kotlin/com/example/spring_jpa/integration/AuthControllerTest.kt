package com.example.spring_jpa.integration

import com.example.spring_jpa.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@Transactional
@Rollback
class AuthControllerTest : AbstractIntegrationTest() {

    private fun registerBody(
        email: String = "user@example.com",
        password: String = "password123",
        name: String = "Test User"
    ) = """{"email":"$email","password":"$password","name":"$name"}"""

    @Test
    fun `POST register with valid data returns 201 and token`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.email").value("user@example.com"))
            .andExpect(jsonPath("$.role").value("USER"))
    }

    @Test
    fun `POST register with duplicate email returns 409`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody())
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody())
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `POST register with invalid email returns 400`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email = "not-an-email"))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.email").exists())
    }

    @Test
    fun `POST register with short password returns 400`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(password = "123"))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.password").exists())
    }

    @Test
    fun `POST login with valid credentials returns 200 and token`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email = "login@example.com", password = "secret123"))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"login@example.com","password":"secret123"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.email").value("login@example.com"))
    }

    @Test
    fun `POST login with wrong password returns 401`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(email = "wrong@example.com", password = "correctPass"))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"wrong@example.com","password":"wrongPass"}""")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    @Test
    fun `POST login with non-existent email returns 401`() {
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"nobody@example.com","password":"password123"}""")
        )
            .andExpect(status().isUnauthorized)
    }
}
