package com.example.spring_jpa.infrastructure.jpa.entity

import com.example.spring_jpa.domain.model.Restaurant
import jakarta.persistence.*

@Entity
@Table(name = "restaurants")
class RestaurantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var address: String,

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    var dishes: List<DishEntity> = emptyList()
) {
    fun toDomain() = Restaurant(id ?: 0, name, address)
    companion object {
        fun fromDomain(restaurant: Restaurant) = RestaurantEntity(
            id = restaurant.id.takeIf { it > 0 },
            name = restaurant.name,
            address = restaurant.address
        )
    }
}
