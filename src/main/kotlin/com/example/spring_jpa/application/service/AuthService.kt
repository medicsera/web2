package com.example.spring_jpa.application.service

import com.example.spring_jpa.application.dto.AuthResponse
import com.example.spring_jpa.application.dto.LoginRequest
import com.example.spring_jpa.application.dto.RegisterRequest
import com.example.spring_jpa.application.exception.AlreadyExistsException
import com.example.spring_jpa.application.exception.NotFoundException
import com.example.spring_jpa.domain.model.Role
import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.UserRepositoryPort
import com.example.spring_jpa.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepositoryPort,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw AlreadyExistsException("User with email ${request.email} already exists")
        }
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password) ?: "",
            firstName = request.name,
            lastName = "",
            role = Role.USER
        )
        val saved = userRepository.create(user)
        logger.info("Registered user: {}", saved.email)
        val token = jwtService.generateToken(saved.email, saved.role.name)
        return AuthResponse(token, saved.email, saved.role.name)
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        val user = userRepository.findByEmail(request.email)
            ?: throw NotFoundException("User not found")
        logger.info("User logged in: {}", user.email)
        val token = jwtService.generateToken(user.email, user.role.name)
        return AuthResponse(token, user.email, user.role.name)
    }
}
