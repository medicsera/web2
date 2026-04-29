package com.example.spring_jpa.exception

sealed class AppException : Exception() {
    data class NotFoundException(val message: String) : AppException()
    data class AlreadyExistsException(val message: String) : AppException()
    data class InvalidOrderStateException(val message: String) : AppException()
}
