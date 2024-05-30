package io.alex.bank.error

sealed class Failure {
    abstract val message: String

    data class CustomerNotFound(override val message: String = "Customer not found") : Failure()
}
