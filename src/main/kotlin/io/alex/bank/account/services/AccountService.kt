package io.alex.bank.account.services

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.alex.bank.account.models.Account
import io.alex.bank.account.models.Currency
import io.alex.bank.account.repositories.AccountRepository
import io.alex.bank.customer.services.CustomerService
import io.alex.bank.error.Failure
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val customerService: CustomerService,
) {
    fun getAccounts(
        pageSize: Int,
        page: Int,
    ) = accountRepository.getAccounts(pageSize = pageSize, page = page)

    fun getAccountsByCustomerId(customerId: UUID): Either<Failure, List<Account>> =
        either {
            val customer = customerService.getCustomer(customerId)

            if (customer.isLeft()) {
                val error =
                    customer.fold(
                        ifLeft = { it },
                        ifRight = { throw IllegalStateException("Customer should not be right") },
                    )

                return error.left()
            }

            val accounts = accountRepository.getAccountsByCustomerId(customerId)

            return accounts.right()
        }

    fun getAccount(accountId: UUID): Either<Failure, Account> =
        either {
            val account = accountRepository.findAccount(accountId) ?: return Failure.AccountNotFound(accountId).left()

            return account.right()
        }

    fun createAccount(account: Account) = accountRepository.createAccount(account)

    fun updateAccount(account: Account): Either<Failure, Account> =
        either {
            val account = accountRepository.updateAccount(account) ?: return Failure.AccountNotFound(account.id).left()

            return account.right()
        }

    fun deposit(
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        version: Int,
    ): Either<Failure, Account> =
        either {
            val account = accountRepository.findAccount(accountId) ?: return Failure.AccountNotFound(accountId).left()

            if (account.currency != currency) {
                return Failure.MismatchedCurrency(account.currency, currency).left()
            }

            if (amount <= 0.toBigDecimal()) {
                return Failure.InvalidDepositAmount(amount).left()
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
                ) ?: return Failure.AccountVersionOutOfDate(accountId).left()

            return updatedAccount.right()
        }

    fun withdraw(
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        version: Int,
    ): Either<Failure, Account> {
        val account = accountRepository.findAccount(accountId) ?: return Failure.AccountNotFound(accountId).left()

        if (account.currency != currency) {
            return Failure.MismatchedCurrency(account.currency, currency).left()
        }

        if (amount <= 0.toBigDecimal()) {
            return Failure.InvalidWithdrawAmount(amount).left()
        }

        if (account.balance < amount) {
            return Failure.InsufficientFunds(accountId).left()
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
            ) ?: return Failure.AccountVersionOutOfDate(accountId).left()

        return updatedAccount.right()
    }

    fun deleteAccount(accountId: UUID): Either<Failure, Unit> {
        val deleteResult = accountRepository.deleteAccount(accountId)

        if (deleteResult == 0) {
            return Failure.AccountNotFound(accountId).left()
        }

        return Unit.right()
    }
}
