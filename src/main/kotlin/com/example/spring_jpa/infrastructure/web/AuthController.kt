package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.AuthResponse
import com.example.spring_jpa.application.dto.LoginRequest
import com.example.spring_jpa.application.dto.RegisterRequest
import com.example.spring_jpa.application.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Аутентификация и регистрация")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
        ApiResponse(responseCode = "400", description = "Некорректные данные или пользователь уже существует")
    ])
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Успешная авторизация, возвращает JWT токен"),
        ApiResponse(responseCode = "401", description = "Неверные учётные данные")
    ])
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(request))
}
