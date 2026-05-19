package com.example.spring_jpa.config

import com.example.spring_jpa.security.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            authorizeHttpRequests {
                authorize("/auth/**", permitAll)
                authorize(HttpMethod.GET, "/api/v1/restaurants/**", permitAll)
                authorize(HttpMethod.GET, "/api/v1/dishes/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            exceptionHandling {
                authenticationEntryPoint = jsonAuthenticationEntryPoint()
                accessDeniedHandler = jsonAccessDeniedHandler()
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }
        return http.build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    private fun jsonAuthenticationEntryPoint() =
        AuthenticationEntryPoint { _: HttpServletRequest, response: HttpServletResponse, _: AuthenticationException ->
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"status":401,"message":"Authentication required"}""")
        }

    private fun jsonAccessDeniedHandler() =
        AccessDeniedHandler { _: HttpServletRequest, response: HttpServletResponse, _: AccessDeniedException ->
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"status":403,"message":"Access denied"}""")
        }
}
