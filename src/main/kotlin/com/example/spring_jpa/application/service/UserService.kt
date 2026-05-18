package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepositoryPort
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getAll(): List<UserResponse> =
        userRepository.findAll().map { UserResponse.fromDomain(it) }

    fun getById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            ?: run {
                logger.warn("User not found with id: {}", id)
                throw NotFoundException("User not found with id: $id")
            }
        return UserResponse.fromDomain(user)
    }

    fun create(request: CreateUserRequest): UserResponse {
        val user = User(
            id = 0,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive ?: true
        )
        val created = userRepository.create(user)
        logger.info("Created user with id: {}", created.id)
        return UserResponse.fromDomain(created)
    }

    fun update(id: Long, request: UpdateUserRequest): UserResponse {
        val existingUser = userRepository.findById(id)
            ?: run {
                logger.warn("User not found with id: {}", id)
                throw NotFoundException("User not found with id: $id")
            }
        val updatedUser = existingUser.copy(
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive ?: true
        )
        val saved = userRepository.update(updatedUser)
            ?: throw NotFoundException("User not found with id: $id")
        logger.info("Updated user with id: {}", id)
        return UserResponse.fromDomain(saved)
    }

    fun delete(id: Long) {
        userRepository.findById(id)
            ?: run {
                logger.warn("User not found with id: {}", id)
                throw NotFoundException("User not found with id: $id")
            }
        userRepository.deleteById(id)
        logger.info("Deleted user with id: {}", id)
    }

    fun getByEmail(email: String): UserResponse? =
        userRepository.findByEmail(email)?.let { UserResponse.fromDomain(it) }
}
