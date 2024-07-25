package io.alex.bank.error

import arrow.core.Either
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException

class ApiException(
    status: HttpStatus,
    message: String? = "Something went wrong",
) : ResponseStatusException(status, message)

@Throws(ApiException::class)
fun <A : Failure, B> Either<A, B>.toResponseOrThrow(
    leftToThrowing: (A) -> Nothing = ::handleFailure,
    status: HttpStatus = OK,
): ResponseEntity<B> =
    fold(
        ifLeft = leftToThrowing,
        ifRight = { value -> ResponseEntity.status(status).body(value) },
    )

fun handleFailure(failure: Failure): Nothing =
    with(failure) {
        when (this) {
            is Failure.CustomerNotFound -> throw ApiException(HttpStatus.NOT_FOUND, message)
            is Failure.AccountNotFound -> throw ApiException(HttpStatus.NOT_FOUND, message)
            is Failure.MismatchedCurrency -> throw ApiException(HttpStatus.BAD_REQUEST, message)
            is Failure.InsufficientFunds -> throw ApiException(HttpStatus.BAD_REQUEST, message)
            is Failure.InvalidDepositAmount -> throw ApiException(HttpStatus.BAD_REQUEST, message)
            is Failure.InvalidWithdrawAmount -> throw ApiException(HttpStatus.BAD_REQUEST, message)
            is Failure.AccountVersionOutOfDate -> throw ApiException(HttpStatus.BAD_REQUEST, message)
            is Failure.ActiveCustomerAnonymization -> throw ApiException(HttpStatus.BAD_REQUEST, message)
            is Failure.ThirdPartyCreditScoreRetrievalFailureClient -> throw ApiException(HttpStatus.BAD_REQUEST, message)
            is Failure.ThirdPartyCreditScoreRetrievalFailureServer -> throw ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message)
        }
    }

@ControllerAdvice
class UncaughtApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(err: NoSuchElementException): ResponseEntity<ApiException> =
        ResponseEntity(ApiException(message = err.message, status = HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(err: IllegalArgumentException): ResponseEntity<ApiException> =
        ResponseEntity(ApiException(message = err.message, status = HttpStatus.NOT_FOUND), HttpStatus.BAD_REQUEST)
}
