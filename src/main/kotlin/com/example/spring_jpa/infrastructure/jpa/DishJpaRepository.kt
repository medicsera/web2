package com.example.spring_jpa.infrastructure.jpa

import com.example.spring_jpa.domain.model.Dish
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface DishJpaRepository : JpaRepository<DishEntity, Long> {
    @Query(
        """
            SELECT d FROM DishEntity d
            WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :namePart, '%'))
            ORDER BY d.name ASC
        """
    )
    fun  findByNameContaining(namePart: String): List<DishEntity>
    fun findByName(name: String): Optional<DishEntity>
}