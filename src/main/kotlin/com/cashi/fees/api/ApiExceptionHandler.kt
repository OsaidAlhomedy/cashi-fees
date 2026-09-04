package com.cashi.fees.api

import com.cashi.fees.api.dto.ApiError
import dev.restate.client.IngressException
import dev.restate.sdk.common.TerminalException
import kotlinx.coroutines.TimeoutCancellationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

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

    @ExceptionHandler(NoResourceFoundException::class)
    fun onNotFound(e: NoResourceFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError("NOT_FOUND", "No endpoint for this path"))

    @ExceptionHandler(IngressException::class)
    fun onIngress(e: IngressException): ResponseEntity<ApiError> {
        val status = e.statusCode
        return if (status in 400..499) {
            ResponseEntity.status(status).body(
                ApiError(
                    error = if (status == 409) "FEE_ALREADY_PROCESSED" else "WORKFLOW_REJECTED",
                    message = if (status == 409)
                        "A fee has already been processed for this transaction"
                    else
                        "The fee workflow rejected this request",
                )
            )
        } else {
            log.error("Restate ingress failure", e)
            ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiError("WORKFLOW_UNAVAILABLE", "Could not reach the fee workflow"))
        }
    }

    @ExceptionHandler(TimeoutCancellationException::class)
    fun onTimeout(e: TimeoutCancellationException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
            .body(ApiError("FEE_STILL_PROCESSING", "Fee workflow did not complete in time; it is still running"))

}