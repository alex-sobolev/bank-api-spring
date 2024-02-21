package com.wolt.wm.training.bank.account.models

import com.wolt.wm.training.bank.customer.models.Customer
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

enum class AccountType {
    SAVINGS,
    CHECKING,
    CREDIT,
}

enum class AccountStatus {
    ACTIVE,
    INACTIVE,
}

enum class Currency {
    EUR,
    USD,
    GBP,
    CHF,
}

data class Account(
    val id: UUID,
    val customerId: UUID,
    val balance: BigDecimal,
    val currency: Currency,
    val type: AccountType,
    val status: AccountStatus,
    val createdAt: LocalDate,
    val updatedAt: LocalDate?,
)

enum class AccountTransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
}

enum class AccountTransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
}

data class AccountTransaction(
    val id: UUID,
    val accountId: UUID,
    val amount: BigDecimal,
    val currency: Currency,
    val type: AccountTransactionType,
    val status: AccountTransactionStatus,
    val createdAt: LocalDate,
)

data class ApiAccountListPage(
    val accounts: List<Account>,
    val page: Int,
    val pageSize: Int,
)

data class ApiAccount(
    val account: Account,
    val customer: Customer,
)

data class ApiAccountTransactionListPage(
    val account: Account,
    val customer: Customer,
    val transactions: List<AccountTransaction>,
    val page: Int,
    val pageSize: Int,
)

data class ApiAccountTransaction(
    val transaction: AccountTransaction,
    val account: Account,
    val customer: Customer,
)

data class ApiCustomerAccountList(
    val accounts: List<Account>,
    val customer: Customer,
)

data class CreateAccountRequest(
    val customerId: String,
    val currency: Currency,
    val type: AccountType,
)

data class AccountDepositRequest(
    val accountId: String,
    val amount: BigDecimal,
    val currency: Currency,
)

data class AccountWithdrawRequest(
    val accountId: String,
    val amount: BigDecimal,
    val currency: Currency,
)
