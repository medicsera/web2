package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.*
import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepositoryPort
){
    fun getAll(): List<UserResponse> =
        userRepository.findAll().map { UserResponse.fromDomain(it) }

    fun getById(id: Long): UserResponse? =
        userRepository.findById(id)?.let { UserResponse.fromDomain(it) }

    fun create(request: CreateUserRequest): UserResponse {
        val user = User(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive
            )
        return UserResponse.fromDomain(userRepository.create(user))
    }

    fun update(id: Long, request: UpdateUserRequest): UserResponse? {
        val user = User(
            id = id,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            isActive = request.isActive
        )
        return userRepository.update(id,user)?.let { UserResponse.fromDomain(it) }
    }

    fun delete(id: Long): Boolean = userRepository.deleteById(id)
}