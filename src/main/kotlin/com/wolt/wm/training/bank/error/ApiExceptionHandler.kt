package com.wolt.wm.training.bank.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.NoSuchElementException

@ControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(err: NoSuchElementException): ResponseEntity<ApiErrorResponseBody> =
        ResponseEntity(ApiErrorResponseBody(error = err.message, status = 404), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(err: IllegalArgumentException): ResponseEntity<ApiErrorResponseBody> =
        ResponseEntity(ApiErrorResponseBody(error = err.message, status = 400), HttpStatus.BAD_REQUEST)
}
