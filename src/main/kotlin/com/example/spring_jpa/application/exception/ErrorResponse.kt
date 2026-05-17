package com.example.spring_jpa.application.exception

import org.springframework.http.HttpStatus

data class ErrorResponse(
    val status: Int,
    val message: String?
) {
    constructor(httpStatus: HttpStatus, message: String?) : this(httpStatus.value(), message)
}

data class ValidationErrorResponse(
    val status: Int,
    val message: String,
    val errors: Map<String, String>
) {
    constructor(httpStatus: HttpStatus, message: String, errors: Map<String, String>) :
            this(httpStatus.value(), message, errors)
}
