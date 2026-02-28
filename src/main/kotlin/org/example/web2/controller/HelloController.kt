package org.example.web2.controller

import org.example.web2.dto.GreetingMain
import org.example.web2.dto.GreetingUser
import org.example.web2.dto.UserData
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/greeting")
class GreetingController {

    // Хранилище пользователей в памяти
    private val users = ConcurrentHashMap<UUID, UserData>()

    /**
     * GET /greeting
     * Обрабатывает два случая:
     * 1. Без параметра id -> возвращает GreetingMain ("Hello World")
     *    (Скрипт Тест 1 проверяет поле .text == "Hello World")
     * 2. С параметром id -> ищет пользователя
     *    (Скрипт Тест 3 проверяет этот кейс)
     */
    @GetMapping
    fun getGreeting(@RequestParam(required = false) id: UUID?): ResponseEntity<Any> {
        return if (id != null) {
            val user = users[id]
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.ok(GreetingMain())
        }
    }

    /**
     * POST /greeting
     * Создает пользователя и возвращает его ID.
     * (Скрипт Тест 2 парсит поле .id из ответа этого метода)
     */
    @PostMapping
    fun createUser(@RequestBody request: UserData): ResponseEntity<GreetingUser> {
        val newId = UUID.randomUUID()
        users[newId] = request

        return ResponseEntity.ok(
            GreetingUser(
                text = "Hello, ${request.surname} ${request.name}",
                id = newId
            )
        )
    }

    /**
     * GET /greeting/{id}
     * Альтернативный способ получения пользователя через путь.
     * (Скрипт Тест 2 проверяет этот кейс сразу после получения ID)
     */
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<UserData> {
        val user = users[id]
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        return ResponseEntity.ok(user)
    }
}
