package com.example.spring_jpa.security

import com.example.spring_jpa.infrastructure.jpa.repository.UserJpaRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userJpaRepository: UserJpaRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userJpaRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")
}
