package com.example.spring_jpa.application.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
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

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ErrorResponse> {
        logger.warn("Bad Request Exception: {}", ex.message)
        return ResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        logger.warn("Bad Credentials: {}", ex.message)
        return ResponseEntity(ErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password"), HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        logger.warn("Access Denied: {}", ex.message)
        return ResponseEntity(ErrorResponse(HttpStatus.FORBIDDEN, "Access denied"), HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .groupBy { it.field }
            .mapValues { (_, errs) -> errs.first().defaultMessage ?: "" }
        logger.warn("Validation Error: {}", errors)
        return ResponseEntity(ValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: {}", ex.message, ex)
        return ResponseEntity(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
