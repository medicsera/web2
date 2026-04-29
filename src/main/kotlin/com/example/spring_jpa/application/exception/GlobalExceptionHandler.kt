package com.example.spring_jpa.application.exception

import com.example.spring_jpa.domain.model.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Not Found Exception: {}", ex.message)
        return ResponseEntity(ErrorResponse(HttpStatus.NOT_FOUND, ex.message), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExistsException(ex: AlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn("Already Exists Exception: {}", ex.message)
        return ResponseEntity(ErrorResponse(HttpStatus.CONFLICT, ex.message), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InvalidOrderStateException::class)
    fun handleInvalidOrderStateException(ex: InvalidOrderStateException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid Order State Exception: {}", ex.message)
        return ResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { FieldError(it.objectName, it.field, it.defaultMessage) }
        logger.warn("Validation Error: {}", errors.joinToString(", "))
        return ResponseEntity(ValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: {}", ex.message, ex)
        return ResponseEntity(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
