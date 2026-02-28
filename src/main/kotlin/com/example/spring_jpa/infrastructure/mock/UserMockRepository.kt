package com.example.spring_jpa.infrastructure.mock

import com.example.spring_jpa.domain.model.User
import com.example.spring_jpa.domain.port.UserRepositoryPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("mock")
class UserMockRepository : UserRepositoryPort{
    private val storage = mutableMapOf<Long, User>()
    private var sequence = 1L

    override fun findAll(): List<User> = storage.values.toList()
    override fun findById(id: Long): User? = storage[id]
    override fun create(user: User): User {
        val saved = user.copy(id = sequence++)
        storage[saved.id] = saved
        return saved
    }

    override fun update(id: Long, user: User): User? {
        if (!storage.containsKey(id)) return null
        val updated = user.copy(id = id)
        storage[id] = updated
        return updated
    }

    override fun deleteById(id: Long): Boolean = storage.remove(id) != null

    override fun existsByEmail(email: String): Boolean =
        storage.values.any { it.email == email
    }
}