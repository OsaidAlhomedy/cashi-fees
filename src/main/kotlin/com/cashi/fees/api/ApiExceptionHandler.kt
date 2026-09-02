package com.cashi.fees.api

import com.cashi.fees.api.dto.ApiError
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun onUnexpected(e: Exception): ResponseEntity<ApiError> {
        log.error("Unhandled error", e)
        return ResponseEntity.internalServerError()
            .body(ApiError("INTERNAL_ERROR", "Unexpected error"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val detail = e.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return ResponseEntity.badRequest().body(ApiError("VALIDATION_FAILED", detail))
    }
}