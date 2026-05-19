package com.example.spring_jpa.integration

import com.example.spring_jpa.AbstractIntegrationTest
import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.service.RestaurantService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@Transactional
@Rollback
class RestaurantControllerTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var restaurantService: RestaurantService

    private fun createRestaurant(name: String = "Test Restaurant", address: String = "Test Address") =
        restaurantService.create(CreateRestaurantRequest(name = name, address = address))

    // --- GET endpoints (public, no auth needed) ---

    @Test
    fun `GET all restaurants returns 200 and list`() {
        createRestaurant()

        mockMvc.perform(get("/api/v1/restaurants"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].name").exists())
    }

    @Test
    fun `GET restaurant by id returns 200 and correct data`() {
        val restaurant = createRestaurant(name = "Sushi Bar")

        mockMvc.perform(get("/api/v1/restaurants/${restaurant.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(restaurant.id))
            .andExpect(jsonPath("$.name").value("Sushi Bar"))
            .andExpect(jsonPath("$.address").value("Test Address"))
    }

    @Test
    fun `GET restaurant with non-existent id returns 404`() {
        mockMvc.perform(get("/api/v1/restaurants/999999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    fun `GET dishes by restaurant id returns 200 and empty list`() {
        val restaurant = createRestaurant()

        mockMvc.perform(get("/api/v1/restaurants/${restaurant.id}/dishes"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `GET dishes with non-existent restaurant id returns 404`() {
        mockMvc.perform(get("/api/v1/restaurants/999999/dishes"))
            .andExpect(status().isNotFound)
    }

    // --- POST (ADMIN only) ---

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST create restaurant as ADMIN returns 201`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "New Cafe", "address": "456 Oak Ave"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("New Cafe"))
            .andExpect(jsonPath("$.address").value("456 Oak Ave"))
            .andExpect(jsonPath("$.id").isNumber)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST with blank name returns 400 validation error`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "", "address": "Some Address"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.name").exists())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `POST with duplicate name returns 409 conflict`() {
        createRestaurant(name = "Unique Bistro")

        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Unique Bistro", "address": "Different Address"}""")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").exists())
    }

    // --- PUT (ADMIN only) ---

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `PUT update restaurant returns 200 and updated data`() {
        val restaurant = createRestaurant(name = "Old Name")

        mockMvc.perform(
            put("/api/v1/restaurants/${restaurant.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "New Name", "address": "New Address"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Name"))
            .andExpect(jsonPath("$.address").value("New Address"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `PUT with non-existent id returns 404`() {
        mockMvc.perform(
            put("/api/v1/restaurants/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Name", "address": "Address"}""")
        )
            .andExpect(status().isNotFound)
    }

    // --- DELETE (ADMIN only) ---

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `DELETE restaurant returns 204`() {
        val restaurant = createRestaurant(name = "To Delete")

        mockMvc.perform(delete("/api/v1/restaurants/${restaurant.id}"))
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `DELETE with non-existent id returns 404`() {
        mockMvc.perform(delete("/api/v1/restaurants/999999"))
            .andExpect(status().isNotFound)
    }

    // --- Security tests ---

    @Test
    fun `POST create restaurant without token returns 401`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Test", "address": "Address"}""")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `POST create restaurant as USER returns 403`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Test", "address": "Address"}""")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `POST create restaurant with invalid token returns 401`() {
        mockMvc.perform(
            post("/api/v1/restaurants")
                .header("Authorization", "Bearer invalid.token.here")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Test", "address": "Address"}""")
        )
            .andExpect(status().isUnauthorized)
    }
}
