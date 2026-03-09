package com.example.spring_jpa.infrastructure.jpa.entity

import com.example.spring_jpa.domain.model.Dish
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "dishes")
class DishEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val description: String = "",

    @Column(nullable = false)
    val price: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val isAvailable: Boolean = true,
)   {
    fun toDomain(): Dish = Dish(
        id = id,
        name = name,
        description = description,
        price = price,
        isAvailable = isAvailable,
    )

    companion object {
        fun fromDomain(dish: Dish): DishEntity = DishEntity(
            id = dish.id,
            name = dish.name,
            description = dish.description,
            price = dish.price,
            isAvailable = dish.isAvailable,
        )
    }
}