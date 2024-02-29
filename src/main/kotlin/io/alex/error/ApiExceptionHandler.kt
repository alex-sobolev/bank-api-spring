package io.alex.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.NoSuchElementException

data class ApiErrorResponseBody(
    val error: String? = "Something went wrong",
    val status: Int,
)

@ControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(err: NoSuchElementException): ResponseEntity<ApiErrorResponseBody> =
        ResponseEntity(ApiErrorResponseBody(error = err.message, status = 404), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(err: IllegalArgumentException): ResponseEntity<ApiErrorResponseBody> =
        ResponseEntity(ApiErrorResponseBody(error = err.message, status = 400), HttpStatus.BAD_REQUEST)
}
