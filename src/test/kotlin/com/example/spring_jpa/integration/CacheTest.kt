package com.example.spring_jpa.integration

import com.example.spring_jpa.AbstractIntegrationTest
import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.service.RestaurantService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@Transactional
@Rollback
class CacheTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var cacheManager: CacheManager

    @Autowired
    lateinit var restaurantService: RestaurantService

    @Test
    fun `repeated GET restaurants does not hit DB on second request`() {
        restaurantService.create(CreateRestaurantRequest(name = "Cache Test Place", address = "Addr 1"))

        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)

        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `creating restaurant evicts restaurants cache`() {
        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)
        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))

        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "New Cache Place", "address": "New Addr"}""")
        ).andExpect(status().isCreated)

        assertNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `deleting restaurant evicts both restaurants and dishes caches`() {
        val restaurant = restaurantService.create(CreateRestaurantRequest(name = "To Delete Cache", address = "Addr"))

        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/restaurants/${restaurant.id}/dishes")).andExpect(status().isOk)

        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
        assertNotNull(cacheManager.getCache("dishes")?.get(restaurant.id))

        mockMvc.perform(delete("/api/v1/restaurants/${restaurant.id}"))
            .andExpect(status().isNoContent)

        assertNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
        assertNull(cacheManager.getCache("dishes")?.get(restaurant.id))
    }

    @Test
    fun `GET restaurant by id is cached on second request`() {
        val restaurant = restaurantService.create(CreateRestaurantRequest(name = "Id Cache Test", address = "Addr"))

        mockMvc.perform(get("/api/v1/restaurants/${restaurant.id}")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/restaurants/${restaurant.id}")).andExpect(status().isOk)

        assertNotNull(cacheManager.getCache("restaurants")?.get(restaurant.id))
    }
}
