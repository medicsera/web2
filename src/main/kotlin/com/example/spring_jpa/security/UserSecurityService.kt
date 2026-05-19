package com.example.spring_jpa.security

import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.springframework.stereotype.Component

@Component("userSecurity")
class UserSecurityService(
    private val userRepository: UserRepositoryPort
) {
    fun isCurrentUser(userId: Long, email: String): Boolean =
        userRepository.findByEmail(email)?.id == userId
}
