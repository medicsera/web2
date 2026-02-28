package com.example.spring_jpa.infrastructure.jpa

import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("db")
class UserJpaAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepositoryPort {

    override fun findAll(): List<User> =
        userJpaRepository.findAll().map { it.toDomain() }

    override fun findById(id: Long): User? =
        userJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun create(user: User): User {
        val entity = UserEntity.fromDomain(user.copy(id = 0))
        return userJpaRepository.save(entity).toDomain()
    }

    override fun update(id: Long, user: User): User? {
        if (!userJpaRepository.existsById(id)) return null
        val entity = UserEntity.fromDomain(user.copy(id = id))
        return userJpaRepository.save(entity).toDomain()
    }

    override fun deleteById(id: Long): Boolean {
        if (!userJpaRepository.existsById(id)) return false
        userJpaRepository.deleteById(id)
        return true
    }

    override fun existsByEmail(email: String): Boolean =
        userJpaRepository.existsByEmail(email)
}