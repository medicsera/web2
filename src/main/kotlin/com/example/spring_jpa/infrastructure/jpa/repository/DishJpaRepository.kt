package com.example.spring_jpa.infrastructure.jpa.repository

import com.example.spring_jpa.infrastructure.jpa.entity.DishEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable  // ← Правильный импорт!
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DishJpaRepository : JpaRepository<DishEntity, Long> {

    @Query("""
        SELECT d FROM DishEntity d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :namePart, '%'))
        ORDER BY d.name ASC
    """)
    fun findByNameContaining(namePart: String): List<DishEntity>

    fun findByName(name: String): DishEntity?

    fun findByRestaurantId(restaurantId: Long, pageable: Pageable): Page<DishEntity>

    fun findByIsAvailableTrue(): List<DishEntity>

    @EntityGraph(attributePaths = ["restaurant"])
    @Query("SELECT d FROM DishEntity d WHERE d.id = :id")
    fun findByIdWithRelations(@Param("id") id: Long): DishEntity?
}