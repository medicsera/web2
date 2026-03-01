package com.example.spring_jpa.config

import com.example.spring_jpa.domain.port.DishRepositoryPort
import com.example.spring_jpa.domain.port.UserRepositoryPort
import com.example.spring_jpa.infrastructure.jpa.*
import com.example.spring_jpa.infrastructure.mock.DishMockRepository
import com.example.spring_jpa.infrastructure.mock.UserMockRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataProviderConfig {

    // ========== MOCK BEANS ==========
    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "mock")
    fun userMockRepository(): UserRepositoryPort = UserMockRepository()

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "mock")
    fun dishMockRepository(): DishRepositoryPort = DishMockRepository()

    // ========== JPA BEANS (по умолчанию) ==========
    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "db", matchIfMissing = true)
    fun userJpaAdapter(userJpaRepository: UserJpaRepository): UserRepositoryPort =
        UserJpaAdapter(userJpaRepository)

    @Bean
    @ConditionalOnProperty(name = ["app.data-provider"], havingValue = "db", matchIfMissing = true)
    fun dishJpaAdapter(dishJpaRepository: DishJpaRepository): DishRepositoryPort =
        DishJpaAdapter(dishJpaRepository)
}
