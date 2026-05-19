package com.example.spring_jpa.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
class CacheConfig {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val objectMapper = ObjectMapper().apply {
            findAndRegisterModules()
            activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType(Any::class.java)
                    .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
            )
        }
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(serializer)

        fun config(ttl: Duration) = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeValuesWith(serializationPair)
            .disableCachingNullValues()

        val cacheWriter = RedisCacheWriter.create(connectionFactory) { it.immediateWrites() }

        return RedisCacheManager.builder(cacheWriter)
            .withCacheConfiguration("restaurants", config(Duration.ofMinutes(10)))
            .withCacheConfiguration("dishes", config(Duration.ofMinutes(10)))
            .cacheDefaults(config(Duration.ofMinutes(5)))
            .build()
    }
}
