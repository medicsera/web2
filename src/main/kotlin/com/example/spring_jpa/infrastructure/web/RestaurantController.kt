package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.CreateDishRequest
import com.example.spring_jpa.application.dto.CreateRestaurantRequest
import com.example.spring_jpa.application.dto.DishResponse
import com.example.spring_jpa.application.dto.RestaurantResponse
import com.example.spring_jpa.application.service.DishService
import com.example.spring_jpa.application.service.RestaurantService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/restaurants")
@Tag(name = "Restaurants", description = "Управление ресторанами")
class RestaurantController(
    private val restaurantService: RestaurantService,
    private val dishService: DishService
) {

    @GetMapping
    @Operation(summary = "Получить список всех ресторанов")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Список ресторанов успешно получен")
    ])
    fun getAll(): ResponseEntity<List<RestaurantResponse>> =
        ResponseEntity.ok(restaurantService.findAll().map { RestaurantResponse.fromDomain(it) })

    @GetMapping("/{id}")
    @Operation(summary = "Получить ресторан по ID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Ресторан найден"),
        ApiResponse(responseCode = "404", description = "Ресторан не найден")
    ])
    fun getById(@PathVariable id: Long): ResponseEntity<RestaurantResponse> =
        ResponseEntity.ok(RestaurantResponse.fromDomain(restaurantService.findById(id)))

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новый ресторан")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Ресторан успешно создан"),
        ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
        ApiResponse(responseCode = "403", description = "Доступ запрещён — требуется роль ADMIN")
    ])
    fun create(@Valid @RequestBody request: CreateRestaurantRequest): ResponseEntity<RestaurantResponse> {
        val restaurant = restaurantService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(RestaurantResponse.fromDomain(restaurant))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateRestaurantRequest
    ): ResponseEntity<RestaurantResponse> =
        ResponseEntity.ok(RestaurantResponse.fromDomain(restaurantService.update(id, request)))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        restaurantService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/dishes")
    fun getDishesByRestaurant(@PathVariable id: Long): ResponseEntity<List<DishResponse>> =
        ResponseEntity.ok(restaurantService.findDishesByRestaurantId(id).map { DishResponse.fromDomain(it) })

    @PostMapping("/{id}/dishes")
    @PreAuthorize("hasRole('ADMIN')")
    fun createDish(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateDishRequest
    ): ResponseEntity<DishResponse> {
        val dish = dishService.create(request.copy(restaurantId = id))
        return ResponseEntity.status(HttpStatus.CREATED).body(DishResponse.fromDomain(dish))
    }
}
