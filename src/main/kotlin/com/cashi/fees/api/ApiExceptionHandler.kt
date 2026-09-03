package com.cashi.fees.api

import com.cashi.fees.api.dto.ApiError
import dev.restate.sdk.common.TerminalException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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

    @ExceptionHandler(TerminalException::class)
    fun onTerminal(e: TerminalException): ResponseEntity<ApiError> {
        val status =
            if (e.code in 400..499) HttpStatus.valueOf(e.code) else HttpStatus.UNPROCESSABLE_ENTITY
        return ResponseEntity.status(status)
            .body(ApiError("FEE_WORKFLOW_REJECTED", e.message ?: "Workflow rejected"))
    }
}