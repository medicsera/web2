package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService (
    private val userRepository: UserRepositoryPort
)  {
    fun getAll(): List<UserResponse> =
        userRepository.findAll().map { UserResponse.fromDomain(it) }

    fun getById(id: Long): UserResponse? =
        userRepository.findById(id)?.let { UserResponse.fromDomain(it) }

    fun create(request: CreateUserRequest): UserResponse {
        val user = User(
            id = 0,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive
            )
        return UserResponse.fromDomain(userRepository.create(user))
    }

    fun update(id: Long, request: UpdateUserRequest): UserResponse? {
        val existingUser = userRepository.findById(id) ?: return null

        val updatedUser = existingUser.copy(
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive
        )

        return userRepository.update(updatedUser)?.let {
            UserResponse.fromDomain(it)
        }
    }

    fun delete(id: Long): Boolean = userRepository.deleteById(id)

    fun getByEmail(email: String): UserResponse? =
        userRepository.findByEmail(email)?.let { UserResponse.fromDomain(it) }
}