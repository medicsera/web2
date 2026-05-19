package com.example.spring_jpa.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(HttpStatus.NOT_FOUND, ex.message), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExistsException(ex: AlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(HttpStatus.CONFLICT, ex.message), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InvalidOrderStateException::class)
    fun handleInvalidOrderStateException(ex: InvalidOrderStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { FieldError(it.objectName, it.field, it.defaultMessage) }
        return ResponseEntity(ValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ErrorResponse(val status: HttpStatus, val message: String)

data class ValidationErrorResponse(
    val status: HttpStatus,
    val message: String,
    val errors: List<FieldError>
)
