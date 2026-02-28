package com.example.spring_jpa.infrastructure.web

import com.example.spring_jpa.application.dto.CreateUserRequest
import com.example.spring_jpa.application.dto.UpdateUserRequest
import com.example.spring_jpa.application.dto.UserResponse
import com.example.spring_jpa.application.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/users")
class UserController (
    private val userService: UserService,
) {
    @GetMapping
    fun getAll(): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(userService.getAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<UserResponse> =
        userService.getById(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    fun create(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> =
        userService.update(id, request)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> =
        if (userService.delete(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
}