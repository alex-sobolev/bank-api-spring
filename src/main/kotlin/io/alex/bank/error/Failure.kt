package io.alex.bank.error

import io.alex.bank.account.models.Currency
import java.math.BigDecimal
import java.util.UUID

sealed class Failure {
    abstract val message: String

    data class CustomerNotFound(
        val customerId: UUID,
    ) : Failure() {
        override val message: String = "Customer with id $customerId not found"
    }

    data class AccountNotFound(
        val accountId: UUID,
    ) : Failure() {
        override val message: String = "Account with id $accountId not found"
    }

    data class AccountVersionOutOfDate(
        val accountId: UUID,
    ) : Failure() {
        override val message: String = "Request has an outdated version for account $accountId"
    }

    data class MismatchedCurrency(
        val expectedCurrency: Currency,
        val suppliedCurrency: Currency,
    ) : Failure() {
        override val message: String = "Mismatched currency: Expected currency $expectedCurrency, but got $suppliedCurrency"
    }

    data class InsufficientFunds(
        val accountId: UUID,
    ) : Failure() {
        override val message: String = "Insufficient funds in account with id $accountId"
    }

    data class InvalidDepositAmount(
        val amount: BigDecimal,
    ) : Failure() {
        override val message: String = "Deposit amount must be positive, but received $amount"
    }

    data class InvalidWithdrawAmount(
        val amount: BigDecimal,
    ) : Failure() {
        override val message: String = "Withdraw amount must be positive, but received $amount"
    }

    data class ActiveCustomerAnonymization(
        val customerId: UUID,
    ) : Failure() {
        override val message: String = "Cannot anonymize an active customer: a customer with id $customerId is still active"
    }

    data class ThirdPartyCreditScoreRetrievalFailureClient(
        val providerName: String,
        val msg: String,
    ) : Failure() {
        override val message: String = "Failed to retrieve credit score from third party: $providerName: $msg"
    }

    data class ThirdPartyCreditScoreRetrievalFailureServer(
        val providerName: String,
        val msg: String,
    ) : Failure() {
        override val message: String = "Failed to retrieve credit score from third party: $providerName: $msg"
    }
}
