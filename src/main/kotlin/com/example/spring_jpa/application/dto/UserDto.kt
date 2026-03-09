package com.example.spring_jpa.application.dto

import com.example.spring_jpa.domain.model.User

data class CreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean
)

data class UpdateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean
)

data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean
) {
    companion object {
        fun fromDomain(user: User): UserResponse = UserResponse(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive
        )
    }
}

