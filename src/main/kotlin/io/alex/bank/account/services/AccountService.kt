package io.alex.bank.account.services

import io.alex.bank.account.models.Account
import io.alex.bank.account.models.Currency
import io.alex.bank.account.repositories.AccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class AccountService(private val accountRepository: AccountRepository) {
    fun getAccounts(
        pageSize: Int,
        page: Int,
    ) = accountRepository.getAccounts(pageSize = pageSize, page = page)

    fun getAccountsByCustomerId(customerId: UUID): List<Account> = accountRepository.getAccountsByCustomerId(customerId)

    fun getAccount(accountId: UUID): Account? = accountRepository.findAccount(accountId)

    fun createAccount(account: Account) = accountRepository.createAccount(account)

    fun updateAccount(account: Account) = accountRepository.updateAccount(account)

    fun deposit(
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        version: Int,
    ): Account {
        val account = accountRepository.findAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        if (account.currency != currency) {
            throw IllegalArgumentException("Deposit currency must match account currency")
        }

        if (amount <= 0.toBigDecimal()) {
            throw IllegalArgumentException("Deposit amount must be positive")
        }

        val accountToUpdate =
            account.copy(
                balance = account.balance + amount,
                updatedAt = LocalDate.now(),
                version = version,
            )

        val updatedAccount =
            accountRepository.updateAccount(
                accountToUpdate,
            ) ?: throw IllegalArgumentException("Account version is outdated")

        return updatedAccount
    }

    fun withdraw(
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        version: Int,
    ): Account {
        val account = accountRepository.findAccount(accountId) ?: throw NoSuchElementException("Account with id $accountId not found")

        if (account.currency != currency) {
            throw IllegalArgumentException("Withdraw currency must match account currency")
        }

        if (amount <= 0.toBigDecimal()) {
            throw IllegalArgumentException("Withdraw amount must be positive")
        }

        if (account.balance < amount) {
            throw IllegalArgumentException("Account balance is less than requested amount")
        }

        val accountToUpdate =
            account.copy(
                balance = account.balance - amount,
                updatedAt = LocalDate.now(),
                version = version,
            )

        val updatedAccount =
            accountRepository.updateAccount(
                accountToUpdate,
            ) ?: throw IllegalArgumentException("Account version is outdated")

        return updatedAccount
    }

    fun deleteAccount(accountId: UUID) {
        val deleteResult = accountRepository.deleteAccount(accountId)

        if (deleteResult == 0) {
            throw NoSuchElementException("Account with id $accountId not found")
        }
    }
}
