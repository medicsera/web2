package com.example.spring_jpa.infrastructure.jpa.entity

import com.example.spring_jpa.domain.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(nullable = false)
    val firstName: String = "",

    @Column(nullable = false)
    val lastName: String = "",

    @Column(nullable = false)
    val isActive: Boolean = true
)   {
    fun toDomain(): User = User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        isActive = isActive
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive
        )
    }
}