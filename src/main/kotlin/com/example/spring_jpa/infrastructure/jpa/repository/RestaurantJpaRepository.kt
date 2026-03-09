package com.example.spring_jpa.infrastructure.jpa.repository

import com.example.spring_jpa.infrastructure.jpa.entity.DishEntity
import com.example.spring_jpa.infrastructure.jpa.entity.RestaurantEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RestaurantJpaRepository : JpaRepository<RestaurantEntity, Long> {

    @EntityGraph(attributePaths = ["dishes"])
    @Query("SELECT r FROM RestaurantEntity r WHERE r.id = :id")  // ← Явный запрос!
    fun findByIdWithDishes(@Param("id") id: Long): RestaurantEntity?  // ← @Param для параметра

    // Если нужен метод для поиска блюд ресторана:
    @Query("SELECT d FROM DishEntity d WHERE d.restaurant.id = :restaurantId")
    fun findDishesByRestaurantId(@Param("restaurantId") id: Long): List<DishEntity>
}
