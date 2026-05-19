package com.example.spring_jpa.application.dto

import com.example.spring_jpa.domain.model.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "First name cannot be blank")
    val firstName: String,

    @field:NotBlank(message = "Last name cannot be blank")
    val lastName: String,

    val isActive: Boolean? = null
)

data class UpdateUserRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "First name cannot be blank")
    val firstName: String,

    @field:NotBlank(message = "Last name cannot be blank")
    val lastName: String,

    val isActive: Boolean? = null
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
